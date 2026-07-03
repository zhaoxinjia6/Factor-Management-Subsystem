/*
 * stress_test.c - 推箱子服务器压力测试工具
 *
 * 模拟多个并发客户端，发送命令，统计响应时间和成功率
 *
 * 编译: gcc -Wall -O2 -o stress_test stress_test.c -lpthread
 * 使用: ./stress_test <服务器IP> <端口> <并发连接数> [测试时间(秒)]
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <pthread.h>
#include <sys/time.h>
#include <time.h>
#include <signal.h>

/* 配置 */
#define MAX_CONNECTIONS 5000
#define BUF_SIZE 4096
#define MAX_USERNAME 32

/* 颜色定义（用于输出） */
#define COLOR_GREEN   "\033[32m"
#define COLOR_YELLOW  "\033[33m"
#define COLOR_RESET   "\033[0m"

/* 统计 */
static volatile int g_running = 1;
static int g_total_connections = 0;
static int g_successful_logins = 0;
static int g_total_commands = 0;
static int g_successful_commands = 0;
static int g_failed_commands = 0;
static double g_total_response_time = 0.0;
static double g_min_response_time = 999999.0;
static double g_max_response_time = 0.0;

static pthread_mutex_t g_stats_mutex = PTHREAD_MUTEX_INITIALIZER;

/* 时间工具 */
static double get_time_ms(void)
{
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return ts.tv_sec * 1000.0 + ts.tv_nsec / 1000000.0;
}

/* 连接服务器 */
static int connect_server(const char *host, int port)
{
    struct sockaddr_in addr;
    struct hostent *he;
    int sock;

    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) return -1;

    /* 设置超时 */
    struct timeval tv;
    tv.tv_sec = 5;
    tv.tv_usec = 0;
    setsockopt(sock, SOL_SOCKET, SO_RCVTIMEO, &tv, sizeof(tv));
    setsockopt(sock, SOL_SOCKET, SO_SNDTIMEO, &tv, sizeof(tv));

    he = gethostbyname(host);
    if (!he) {
        close(sock);
        return -1;
    }

    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port = htons(port);
    memcpy(&addr.sin_addr, he->h_addr_list[0], he->h_length);

    if (connect(sock, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        close(sock);
        return -1;
    }

    return sock;
}

/* 发送命令并等待响应 */
static int send_and_recv(int sock, const char *cmd, char *resp, int resp_size,
                         double *elapsed)
{
    double start, end;
    int n;
    char buf[BUF_SIZE];

    start = get_time_ms();

    /* 发送命令 */
    char send_buf[BUF_SIZE];
    int len = snprintf(send_buf, sizeof(send_buf), "%s\n", cmd);
    if (write(sock, send_buf, len) != len)
        return -1;

    /* 读取响应 */
    n = (int)read(sock, buf, sizeof(buf) - 1);
    if (n <= 0) return -1;

    buf[n] = '\0';

    end = get_time_ms();

    if (elapsed) *elapsed = end - start;

    if (resp) {
        int cpylen = n < resp_size - 1 ? n : resp_size - 1;
        memcpy(resp, buf, cpylen);
        resp[cpylen] = '\0';
    }

    return 0;
}

/* 客户端模拟线程 */
typedef struct {
    int id;
    const char *host;
    int port;
    int duration;
} ThreadArg;

static void *client_thread(void *arg)
{
    ThreadArg *ta = (ThreadArg *)arg;
    int sock;
    char resp[BUF_SIZE];
    double elapsed, start_time;
    char username[MAX_USERNAME];

    snprintf(username, sizeof(username), "test_%d", ta->id);

    start_time = get_time_ms();

    /* 连接 */
    sock = connect_server(ta->host, ta->port);
    if (sock < 0) {
        pthread_mutex_lock(&g_stats_mutex);
        g_total_connections++;
        pthread_mutex_unlock(&g_stats_mutex);
        return NULL;
    }

    pthread_mutex_lock(&g_stats_mutex);
    g_total_connections++;
    pthread_mutex_unlock(&g_stats_mutex);

    /* 读取欢迎消息 */
    {
        char welcome[BUF_SIZE];
        int n = (int)read(sock, welcome, sizeof(welcome) - 1);
        (void)n;
    }

    /* 发送命令循环 */
    int cmd_count = 0;
    char cmds[][16] = {"LEVELS", "LIST", "WHOAMI", "HELP"};

    while (g_running && (get_time_ms() - start_time) < ta->duration * 1000) {
        /* 登录 */
        char login_cmd[64];
        snprintf(login_cmd, sizeof(login_cmd), "LOGIN %s", username);
        if (send_and_recv(sock, login_cmd, resp, sizeof(resp), &elapsed) == 0) {
            if (strncmp(resp, "OK", 2) == 0 || strncmp(resp, "ERR", 3) == 0) {
                pthread_mutex_lock(&g_stats_mutex);
                g_total_commands++;
                g_successful_commands++;
                g_successful_logins++;
                g_total_response_time += elapsed;
                if (elapsed < g_min_response_time) g_min_response_time = elapsed;
                if (elapsed > g_max_response_time) g_max_response_time = elapsed;
                pthread_mutex_unlock(&g_stats_mutex);
            }
        }

        /* 发送几个命令 */
        for (int i = 0; i < 5 && g_running; i++) {
            const char *cmd = cmds[rand() % 4];

            if (send_and_recv(sock, cmd, NULL, 0, &elapsed) == 0) {
                pthread_mutex_lock(&g_stats_mutex);
                g_total_commands++;
                g_successful_commands++;
                g_total_response_time += elapsed;
                if (elapsed < g_min_response_time) g_min_response_time = elapsed;
                if (elapsed > g_max_response_time) g_max_response_time = elapsed;
                pthread_mutex_unlock(&g_stats_mutex);
            } else {
                pthread_mutex_lock(&g_stats_mutex);
                g_failed_commands++;
                pthread_mutex_unlock(&g_stats_mutex);
                goto done;
            }

            usleep(10000);  /* 10ms 间隔 */
            cmd_count++;
        }

        /* 开始游戏并移动几步 */
        if (send_and_recv(sock, "PLAY 1", NULL, 0, &elapsed) == 0) {
            /* 读取地图数据 */
            char buf[BUF_SIZE];
            for (int i = 0; i < 12; i++) {
                int n = (int)read(sock, buf, sizeof(buf) - 1);
                if (n <= 0) break;
            }

            /* 移动几次 */
            const char *dirs = "wasd";
            for (int i = 0; i < 8; i++) {
                char move_cmd[16];
                snprintf(move_cmd, sizeof(move_cmd), "MOVE %c", dirs[rand() % 4]);
                if (send_and_recv(sock, move_cmd, NULL, 0, &elapsed) == 0) {
                    pthread_mutex_lock(&g_stats_mutex);
                    g_total_commands++;
                    g_successful_commands++;
                    g_total_response_time += elapsed;
                    if (elapsed < g_min_response_time) g_min_response_time = elapsed;
                    if (elapsed > g_max_response_time) g_max_response_time = elapsed;
                    pthread_mutex_unlock(&g_stats_mutex);
                }
                /* 读取地图响应 */
                for (int j = 0; j < 12; j++) {
                    int n = (int)read(sock, buf, sizeof(buf) - 1);
                    if (n <= 0) break;
                }
                usleep(5000);
            }

            send_and_recv(sock, "QUIT", NULL, 0, NULL);
        }

        usleep(100000);  /* 100ms */
    }

done:
    close(sock);
    return NULL;
}

int main(int argc, char *argv[])
{
    const char *host;
    int port;
    int num_clients;
    int duration = 10;  /* 默认测试10秒 */
    pthread_t threads[MAX_CONNECTIONS];
    ThreadArg args[MAX_CONNECTIONS];
    double test_start, test_end;

    if (argc < 3) {
        printf("用法: %s <服务器IP> <端口> <并发连接数> [测试时间(秒)]\n", argv[0]);
        return 1;
    }

    host = argv[1];
    port = atoi(argv[2]);
    num_clients = atoi(argv[3]);

    if (num_clients > MAX_CONNECTIONS) {
        printf("最大并发连接数为 %d\n", MAX_CONNECTIONS);
        num_clients = MAX_CONNECTIONS;
    }

    if (argc > 4) duration = atoi(argv[4]);

    if (port <= 0 || port > 65535) {
        fprintf(stderr, "端口无效\n");
        return 1;
    }

    printf("========================================\n");
    printf("  推箱子服务器压力测试\n");
    printf("========================================\n");
    printf("目标服务器: %s:%d\n", host, port);
    printf("并发连接数: %d\n", num_clients);
    printf("测试时长:   %d 秒\n\n", duration);

    /* 先测试是否能连上 */
    {
        int test_sock = connect_server(host, port);
        if (test_sock < 0) {
            fprintf(stderr, "无法连接到服务器 %s:%d\n", host, port);
            return 1;
        }
        close(test_sock);
        printf("服务器连通性: OK\n\n");
    }

    signal(SIGINT, SIG_IGN);

    srand((unsigned int)time(NULL));

    test_start = get_time_ms();

    /* 创建线程 */
    for (int i = 0; i < num_clients; i++) {
        args[i].id = i;
        args[i].host = host;
        args[i].port = port;
        args[i].duration = duration;
        pthread_create(&threads[i], NULL, client_thread, &args[i]);
    }

    /* 等待指定时间 */
    sleep(duration);

    g_running = 0;

    /* 等待线程结束 */
    for (int i = 0; i < num_clients; i++) {
        pthread_join(threads[i], NULL);
    }

    test_end = get_time_ms();

    /* 输出结果 */
    double test_time = (test_end - test_start) / 1000.0;

    printf("\n========================================\n");
    printf("  测试结果\n");
    printf("========================================\n");
    printf("实际测试时间: %.2f 秒\n", test_time);
    printf("总连接数:     %d\n", g_total_connections);
    printf("成功登录数:   %d\n", g_successful_logins);
    printf("总命令数:     %d\n", g_total_commands);
    printf("成功命令数:   %d\n", g_successful_commands);
    printf("失败命令数:   %d\n", g_failed_commands);
    printf("成功率:       %.2f%%\n",
        g_total_commands > 0 ?
        (double)g_successful_commands / g_total_commands * 100.0 : 0.0);
    printf("命令吞吐量:   %.2f 命令/秒\n",
        test_time > 0 ? g_total_commands / test_time : 0.0);
    printf("平均响应时间: %.3f ms\n",
        g_total_commands > 0 ? g_total_response_time / g_total_commands : 0.0);
    printf("最小响应时间: %.3f ms\n", g_min_response_time);
    printf("最大响应时间: %.3f ms\n", g_max_response_time);
    printf("\n");

    if (g_failed_commands == 0 && g_total_commands > 0) {
        printf(COLOR_GREEN "结论: 服务器在 %d 并发连接下运行稳定，无命令失败\n" COLOR_RESET,
               num_clients);
    } else if (g_failed_commands > 0) {
        printf(COLOR_YELLOW "结论: 服务器在 %d 并发连接下有 %d 个命令失败(%.2f%%)\n" COLOR_RESET,
               num_clients, g_failed_commands,
               g_total_commands > 0 ?
               (double)g_failed_commands / g_total_commands * 100.0 : 0.0);
    }

    return 0;
}
