# Makefile - 推箱子游戏服务器

CC = gcc
CFLAGS = -Wall -Wextra -O2 -g -D_GNU_SOURCE
LDFLAGS = -lpthread

SRCS = server.c game.c levels.c client.c
OBJS = $(SRCS:.c=.o)
TARGET = sokoban_server

.PHONY: all clean test

all: $(TARGET)

$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

%.o: %.c
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	rm -f $(OBJS) $(TARGET)

test: $(TARGET)
	@echo "=== 编译成功，运行服务器 (端口 8888) ==="
	./$(TARGET) 8888
