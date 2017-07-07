precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D vTexture;

uniform float uLandmarkX[106];
uniform float uLandmarkY[106];


bool isLandmark(vec2 point) {
  float alw = 0.005;
  for (int i = 0; i < 106; i++) {
      float fX = 1.0 - uLandmarkX[i] / 480.0;
      float fY = uLandmarkY[i] / 640.0;
      float mX = point.x;
      float mY = point.y;
      if ( (mX >= fX - alw) && (mX <= fX + alw) && (mY >= fY - alw) && (mY <= fY + alw) ) {
        return true;
      }
  }
  return false;
}

void main() {
    vec4 nColor = texture2D(vTexture, textureCoordinate);

    gl_FragColor = (isLandmark(textureCoordinate))
        ? vec4(0.0, 1.0, 0.0, 1.0)
        : nColor;
}