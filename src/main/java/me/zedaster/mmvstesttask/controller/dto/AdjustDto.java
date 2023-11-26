package me.zedaster.mmvstesttask.controller.dto;

public class AdjustDto {
    private int width;
    private int height;

    public AdjustDto(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
