/*
 * client.h - 客户端会话管理
 * 维护每个连接的用户信息、游戏状态、读写缓冲区
 */

#ifndef CLIENT_H
#define CLIENT_H

#include <sys/socket.h>
#include "game.h"

#define BUFFER_SIZE 4096
#define MAX_USERNAME 32

/* 客户端状态 */
typedef enum {
    ST_LOGIN,      /* 已连接但未登录 */
    ST_LOBBY,      /* 已登录，在大厅 */
    ST_PLAYING     /* 正在游戏中 */
} ClientState;

/* 客户端会话 */
typedef struct ClientNode {
    int fd;                        /* socket 文件描述符 */
    struct sockaddr_in addr;       /* 客户端地址 */
    char username[MAX_USERNAME];   /* 用户名 */
    ClientState state;             /* 状态 */
    GameSession game;              /* 游戏会话 */

    /* 读写缓冲区（非阻塞使用） */
    char rbuf[BUFFER_SIZE];        /* 读缓冲区 */
    int rlen;                      /* 读缓冲区中有效数据长度 */
    char wbuf[BUFFER_SIZE];        /* 写缓冲区 */
    int wlen;                      /* 写缓冲区中有效数据长度 */
    int wpos;                      /* 写缓冲区已发送位置 */

    struct ClientNode *next;       /* 链表指针 */
} ClientNode;

/* 客户端管理器（线程安全，由调用者保证） */
typedef struct {
    ClientNode *head;              /* 链表头 */
    int count;                     /* 当前在线客户端数 */
} ClientManager;

/* 初始化客户端管理器 */
void client_manager_init(ClientManager *mgr);

/* 添加客户端 */
ClientNode *client_manager_add(ClientManager *mgr, int fd,
                                const struct sockaddr_in *addr);

/* 通过 fd 查找客户端 */
ClientNode *client_manager_find(ClientManager *mgr, int fd);

/* 通过用户名查找客户端 */
ClientNode *client_manager_find_by_name(ClientManager *mgr, const char *name);

/* 删除客户端 */
void client_manager_remove(ClientManager *mgr, int fd);

/* 获取在线用户名列表（发送给客户端） */
void client_manager_list_users(ClientManager *mgr, char *out, size_t out_size);

/* 广播消息给所有已登录客户端 */
void client_manager_broadcast(ClientManager *mgr, const char *msg,
                              ClientNode *exclude);

/* 获取在线用户数 */
int client_manager_count(const ClientManager *mgr);

/* 释放所有客户端 */
void client_manager_destroy(ClientManager *mgr);

#endif /* CLIENT_H */
