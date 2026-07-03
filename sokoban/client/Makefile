# Makefile - 推箱子游戏客户端

CC = gcc
CFLAGS = -Wall -Wextra -O2 -g
LDFLAGS =

SRCS = client.c
OBJS = $(SRCS:.c=.o)
TARGET = sokoban_client

.PHONY: all clean

all: $(TARGET)

$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

%.o: %.c
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	rm -f $(OBJS) $(TARGET)
