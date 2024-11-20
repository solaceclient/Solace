package huysuh.Utils;

public class AnimationUtil {
    public static float animate(float target, float current, float speed) {
        float diff = target - current;
        if (diff > speed) {
            return current + speed;
        } else if (diff < -speed) {
            return current - speed;
        }
        return target;
    }

    public static float easeInOutQuad(float current, float target, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) return target;

        float change = diff * speed;
        // Added smoothing for quadratic easing
        change *= Math.abs(diff);

        if (change > 0) {
            change = Math.min(change, diff);
        } else {
            change = Math.max(change, diff);
        }

        return current + change;
    }

    public static float easeInOutSine(float current, float target, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) return target;

        // Improved sine easing with better curve
        float progress = current / target;
        float change = (float) (diff * speed * Math.sin(progress * Math.PI / 2));

        if (change > 0) {
            change = Math.min(change, diff);
        } else {
            change = Math.max(change, diff);
        }

        return current + change;
    }

    public static float elasticOut(float current, float target, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) return target;

        // Enhanced elastic effect
        float progress = Math.min(1, current / target);
        float bounce = (float) (Math.sin(progress * Math.PI * 3) * Math.exp(-progress * 4) * 0.5);
        float change = diff * speed + bounce * diff;

        if (current < target) {
            change = Math.min(change, diff);
        } else {
            change = Math.max(change, diff);
        }

        return current + change;
    }

    public static float bounceOut(float current, float target, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) return target;

        // Bounce effect with dampening
        float progress = Math.min(1, current / target);
        float bounce = (float) (Math.abs(Math.sin(progress * Math.PI * 2.5)) * Math.exp(-progress * 2) * 0.4);

        float change = diff * speed;
        if (current < target) {
            change = Math.min(change + (bounce * diff), diff);
        } else {
            change = Math.max(change - (bounce * diff), diff);
        }

        return current + change;
    }

    public static float smoothStep(float current, float target, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) return target;

        // Smooth step interpolation
        float progress = Math.min(1, Math.max(0, current / target));
        progress = progress * progress * (3 - 2 * progress); // Smoothstep formula

        float change = diff * speed * (1 - progress);
        if (change > 0) {
            change = Math.min(change, diff);
        } else {
            change = Math.max(change, diff);
        }

        return current + change;
    }

    // Helper method for spring-based animations
    public static float spring(float current, float target, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) return target;

        // Spring physics simulation
        float springStrength = 10.0f;
        float dampening = 0.8f;

        float acceleration = diff * springStrength;
        float velocity = acceleration * speed * dampening;

        return current + velocity;
    }
}