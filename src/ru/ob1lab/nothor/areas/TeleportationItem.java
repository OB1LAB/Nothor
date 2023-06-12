package ru.ob1lab.nothor.areas;

public class TeleportationItem {
    public String world;
    public int x;
    public int y;
    public int z;
    public float yaw;
    public float pitch;
    public TeleportationItem(String world, int x, int y, int z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
