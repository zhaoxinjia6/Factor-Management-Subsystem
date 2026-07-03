/*
 * client.c - 客户端会话管理实现
 */

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <arpa/inet.h>
#include "client.h"

void client_manager_init(ClientManager *mgr)
{
    mgr->head = NULL;
    mgr->count = 0;
}

ClientNode *client_manager_add(ClientManager *mgr, int fd,
                                const struct sockaddr_in *addr)
{
    ClientNode *node = (ClientNode *)malloc(sizeof(ClientNode));
    if (!node) return NULL;

    node->fd = fd;
    node->addr = *addr;
    node->username[0] = '\0';
    node->state = ST_LOGIN;
    node->game.level = NULL;
    node->game.status = GAME_IDLE;
    node->rlen = 0;
    node->wlen = 0;
    node->wpos = 0;
    node->next = mgr->head;
    mgr->head = node;
    mgr->count++;
    return node;
}

ClientNode *client_manager_find(ClientManager *mgr, int fd)
{
    ClientNode *p = mgr->head;
    while (p) {
        if (p->fd == fd) return p;
        p = p->next;
    }
    return NULL;
}

ClientNode *client_manager_find_by_name(ClientManager *mgr, const char *name)
{
    ClientNode *p = mgr->head;
    while (p) {
        if (p->username[0] && strcmp(p->username, name) == 0)
            return p;
        p = p->next;
    }
    return NULL;
}

void client_manager_remove(ClientManager *mgr, int fd)
{
    ClientNode **pp = &mgr->head;
    while (*pp) {
        if ((*pp)->fd == fd) {
            ClientNode *tmp = *pp;
            *pp = tmp->next;
            free(tmp);
            mgr->count--;
            return;
        }
        pp = &(*pp)->next;
    }
}

void client_manager_list_users(ClientManager *mgr, char *out, size_t out_size)
{
    ClientNode *p = mgr->head;
    size_t pos = 0;

    pos += snprintf(out + pos, out_size - pos, "在线用户 (%d):\n", mgr->count);
    while (p) {
        if (p->state != ST_LOGIN) {
            pos += snprintf(out + pos, out_size - pos,
                "  %s [%s]\n", p->username,
                p->state == ST_PLAYING ? "游戏中" : "大厅");
        }
        p = p->next;
    }
}

void client_manager_broadcast(ClientManager *mgr, const char *msg,
                              ClientNode *exclude)
{
    ClientNode *p = mgr->head;
    size_t len = strlen(msg);
    while (p) {
        if (p != exclude && p->state != ST_LOGIN) {
            /* 如果要写的数据超过缓冲区，丢弃 */
            if (p->wlen + len < BUFFER_SIZE) {
                memcpy(p->wbuf + p->wlen, msg, len);
                p->wlen += (int)len;
            }
        }
        p = p->next;
    }
}

int client_manager_count(const ClientManager *mgr)
{
    return mgr->count;
}

void client_manager_destroy(ClientManager *mgr)
{
    ClientNode *p = mgr->head;
    while (p) {
        ClientNode *tmp = p;
        p = p->next;
        if (tmp->fd >= 0) close(tmp->fd);
        free(tmp);
    }
    mgr->head = NULL;
    mgr->count = 0;
}
