package randommagics.render;

public class ModShaders {
    public static ShaderProgram glitch;
    public static ShaderProgram grey;

    // ���� ����� �� �������� ��� ������������� ����, ��� �� ���������������� ��� ���� �������
    public static void register(){
        // ���� � ������ ������� �� ��������� ������������ ����� ����� � �������
        glitch = new ShaderProgram()
                .addFragment("shaders/glitch.frag") // ��������� �����������
                .addVertex("shaders/glitch.vert") // ��������� ����������
                .compile(); // ����� ���������� ���� �������� ����������� ����������
        
        grey = new ShaderProgram().addFragment("shaders/grey.frag").addVertex("shaders/grey.vert").compile();
    }
}
