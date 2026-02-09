#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;

uniform mat4 mvp;
uniform mat4 model;

out vec3 Normal;
out vec3 WorldPos;

void main()
{
    vec4 world = model * vec4(aPos,1.0);

    WorldPos = world.xyz;

    // correct normal transform
    Normal = mat3(transpose(inverse(model))) * aNormal;

    gl_Position = mvp * vec4(aPos,1.0);
}
