#version 410 core
precision highp float;

in vec4 pass_Position;

vec2 squareImaginary(vec2 imaginaryNum) {
    vec2 imaginaryResult;
    imaginaryResult.x = (imaginaryNum.x * imaginaryNum.x) - (imaginaryNum.y * imaginaryNum.y);
    imaginaryResult.y = 2 * imaginaryNum.x * imaginaryNum.y;
    return imaginaryResult;
}

vec3 colorFunc(int iter) {
    // Color in HSV
    vec3 color = vec3(0.95 + 0.012*iter , 1.0, 0.2+.4*(1.0+sin(0.3*iter)));

    // Convert from HSV to RGB
    // Taken from: http://lolengine.net/blog/2013/07/27/rgb-to-hsv-in-glsl
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 m = abs(fract(color.xxx + K.xyz) * 6.0 - K.www);
    return vec3(color.z * mix(K.xxx, clamp(m - K.xxx, 0.0, 1.0), color.y));
}

void main() {
    vec2 c, z;
    vec3 color = vec3(0.0, 0.0, 0.0);
    c = vec2(pass_Position.x, pass_Position.y);
    z = c;

    int iter;
    for(iter = 0; iter < 100; iter++) {
        //fc(z) = z^2 + c
        vec2 result = squareImaginary(z) + c;
        if(length(result) > 4.0) {
            color = colorFunc(iter);
            break;
        }
        z = result;
    }

    gl_FragColor = vec4(color, 1.0);
}
