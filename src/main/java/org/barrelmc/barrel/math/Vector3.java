package org.barrelmc.barrel.math;

import lombok.Getter;
import lombok.Setter;
import org.cloudburstmc.math.GenericMath;
import org.cloudburstmc.math.vector.Vector3f;

public class Vector3 {

    @Setter
    @Getter
    public double x;
    @Setter
    @Getter
    public double y;
    @Setter
    @Getter
    public double z;
    @Setter
    @Getter
    public float yaw;
    @Setter
    @Getter
    public float pitch;

    public int getFloorX() {
        return GenericMath.floor(this.x);
    }

    public int getFloorY() {
        return GenericMath.floor(this.y);
    }

    public int getFloorZ() {
        return GenericMath.floor(this.z);
    }

    public void setPosition(Vector3f vector3f) {
        this.x = vector3f.getX();
        this.y = vector3f.getY();
        this.z = vector3f.getZ();
    }

    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setLocation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Vector3f getVector3f() {
        return Vector3f.from(this.x, this.y + 1.62, this.z);
    }
}
