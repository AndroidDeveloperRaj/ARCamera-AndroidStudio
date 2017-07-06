precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D vTexture;

void main() {
    vec4 nColor = texture2D(vTexture, textureCoordinate);
    float avg = (nColor.r + nColor.g + nColor.b) / 3.0;
    float binary;
    if (avg >= 0.5) {
        binary = 1.0;
    } else {
        binary = 0.0;
    }
    gl_FragColor = vec4(binary, binary, binary, nColor.a);
}