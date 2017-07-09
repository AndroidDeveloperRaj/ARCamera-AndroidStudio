precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D vTexture;

uniform float uLandmarkX[106];
uniform float uLandmarkY[106];


vec2 bulgePoint(vec2 uv, vec2 center, float factor) {
    vec2 dir = normalize( center - uv );
    float d = length( center - uv );
    float f = exp( factor * ( d - .5 ) ) - 1.;
    if( d > .5 ) f = 0.;
    return f * dir;
}

void main() {
    vec2 uv = textureCoordinate;
    float factor = 0.5 * sin( 3.17);

    vec2 center1 = vec2(uLandmarkX[74], uLandmarkY[74]);
    vec2 center2 = vec2(uLandmarkX[77], uLandmarkY[77]);

    vec2 changPoints = bulgePoint(uv, center1, factor) + bulgePoint(uv, center2, factor);
    gl_FragColor = texture2D( vTexture, uv + changPoints );
}