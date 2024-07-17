package com.easypan.entity.dto;

import java.io.Serializable;

public class UserSpaceDto implements Serializable {
    // 已使用的空间
    private Long useSpace;
    // 总空间
    private Long totalSpace;

    public Long getUseSpace() {
        return useSpace;
    }

    public void setUseSpace(Long useSpace) {
        this.useSpace = useSpace;
    }

    public Long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(Long totalSpace) {
        this.totalSpace = totalSpace;
    }
}
