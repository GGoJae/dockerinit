package com.dockerinit.linux.infrastructure.repository;

public interface LinuxCommandRepositoryCustom {
    void increaseSearchCount(String commandNorm, int delta);
}
