#version 330 core

in vec3 Normal;
in vec3 WorldPos;

uniform vec3 color;
uniform vec3 lightDir;
uniform vec3 lightColor;
uniform vec3 skyColor;
uniform vec3 groundColor;
uniform vec3 viewPos;

out vec4 FragColor;

void main()
{
    vec3 N = normalize(Normal);
    vec3 L = normalize(-lightDir);
    vec3 V = normalize(viewPos - WorldPos);

    // ===== Stylized Diffuse =====
    float diff = max(dot(N,L),0.0);
    diff = smoothstep(0.1,1.0,diff);

    vec3 sun = lightColor * diff;

    // ===== Ambient =====
    float skyAmount = clamp(N.y*0.5+0.5,0.0,1.0);
    vec3 ambient = mix(groundColor,skyColor,skyAmount);

    // ===== Specular =====
    float roughness = 0.55;
    float specPower = mix(64.0,8.0,roughness);

    vec3 H = normalize(L+V);
    float spec = pow(max(dot(N,H),0.0),specPower);

    vec3 specular = lightColor * spec * (1.0-roughness) * 0.6;

    // ===== Rim Light =====
    float rim = 1.0-max(dot(N,V),0.0);
    rim = pow(rim,2.5);

    vec3 rimLight = lightColor * rim * 0.2;

    vec3 lighting =
        color * (ambient * 1.4 + sun * 1.2)
        + specular
        + rimLight;

    // ====================================================
    // ===== FAKE PROJECTED SHADOW ============
    // ====================================================

    // Only apply fake shadow when object is above ground
    float objectHeight = WorldPos.y;

    // ignore terrain (near y = 0)
    float shadow = 0.0;

    if(objectHeight > 0.05)
    {
        shadow = smoothstep(2.0, 0.0, objectHeight);
        lighting *= mix(0.85, 1.0, shadow);
    }


    // ====================================================

    // ===== Tone mapping =====
    lighting = lighting / (lighting + vec3(1.0));

    // ===== Distance Fog =====
    float dist = length(viewPos-WorldPos);
    float fogStart = 12.0;
    float fogEnd   = 45.0;

    float fogFactor = clamp((fogEnd-dist)/(fogEnd-fogStart),0.0,1.0);

    vec3 finalColor = mix(skyColor,lighting,fogFactor);

    FragColor = vec4(finalColor,1.0);
}
