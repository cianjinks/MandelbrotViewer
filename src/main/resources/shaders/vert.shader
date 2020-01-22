#version 330 core

uniform mat4 u_MVP;

in vec4 in_Position;

out vec4 pass_Position;
void main(void) {
	gl_Position = u_MVP * in_Position;

	pass_Position = in_Position;
}