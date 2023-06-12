package ru.ob1lab.nothor.areas;

public class LocationItem {
    public String world;
    public int x1;
    public int y1;
    public int z1;
    public int x2;
    public int y2;
    public int z2;
    public LocationItem(String world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.world = world;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
        this.y2 = y2;
    }
}
