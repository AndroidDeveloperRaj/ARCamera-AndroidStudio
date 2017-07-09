precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D vTexture;

uniform float uLandmarkX[106];
uniform float uLandmarkY[106];


void main() {
    vec2 uv = textureCoordinate;
    float factor = 0.5 * sin( 3.17);

    vec2 center1 = vec2(uLandmarkX[74], uLandmarkY[74]);
    vec2 dir1 = normalize( center1 - uv );
    float d1 = length( center1 - uv );
    float f1 = exp( factor * ( d1 - .5 ) ) - 1.;
    if( d1 > .5 ) f1 = 0.;

    vec2 center2 = vec2(uLandmarkX[77], uLandmarkY[77]);
    vec2 dir2 = normalize( center2 - uv );
    float d2 = length( center2 - uv );
    float f2 = exp( factor * ( d2- .5 ) ) - 1.;
    if( d2 > .5 ) f2 = 0.;

    gl_FragColor = texture2D( vTexture, uv + f1 * dir1 + f2 * dir2 );
}