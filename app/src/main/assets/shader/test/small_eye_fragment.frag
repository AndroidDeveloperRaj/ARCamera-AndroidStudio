precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D vTexture;

uniform float uLandmarkX[106];
uniform float uLandmarkY[106];


vec2 pinchPoint(vec2 uv, vec2 center, float level) {
    vec2 dir = normalize( center - uv );
    float d = length( center - uv );
    float factor = level * sin( 3.1);
    float f = exp( factor * ( d - .5 ) ) - 1.;
    if( d > .5 ) f = 0.;
    return f * dir;
}

void main() {
    vec2 uv = textureCoordinate;

    vec2 leftEyeLeftCorner = vec2(uLandmarkX[52], uLandmarkY[52]);
    vec2 leftEyeCenter = vec2(uLandmarkX[74], uLandmarkY[74]);
    vec2 leftEyeRightCorner = vec2(uLandmarkX[55], uLandmarkY[55]);

    vec2 rightEyeLeftCorner = vec2(uLandmarkX[58], uLandmarkY[58]);
    vec2 rightEyeCenter = vec2(uLandmarkX[77], uLandmarkY[77]);
    vec2 rightEyeRightCorner = vec2(uLandmarkX[61], uLandmarkY[61]);

    vec2 changPoints =
            pinchPoint(uv, leftEyeLeftCorner, 0.5) +
            pinchPoint(uv, leftEyeCenter, 0.25) +
            pinchPoint(uv, leftEyeRightCorner, 1.0) +
            pinchPoint(uv, rightEyeLeftCorner, 1.0) +
            pinchPoint(uv, rightEyeCenter, 0.25) +
            pinchPoint(uv, rightEyeRightCorner, 0.5);
    gl_FragColor = texture2D( vTexture, uv + changPoints );
}