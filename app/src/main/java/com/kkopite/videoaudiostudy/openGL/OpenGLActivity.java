package com.kkopite.videoaudiostudy.openGL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.kkopite.videoaudiostudy.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLActivity extends AppCompatActivity {

    private GLSurfaceView mSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_gl);
        initView();
    }


    private void initView() {
        mSurface = (GLSurfaceView) findViewById(R.id.surface);
        // 版本
        mSurface.setEGLContextClientVersion(2);
        //
        mSurface.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        //  与 renderer连接起来
        mSurface.setRenderer(new PicRenderer(this));
        // 不停渲染
//        GLSurfaceView.RENDERMODE_WHEN_DIRTY, 懒惰渲染, 如要手动调用 requestRender()
        mSurface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurface.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurface.onResume();
    }

    private static class MyRenderer implements GLSurfaceView.Renderer {
        private static final String VERTEX_SHADER =
                "attribute vec4 vPosition;\n"
                        + "uniform mat4 uMVPMatrix;\n"
                        + "void main() {\n"
                        + " gl_Position = uMVPMatrix * vPosition;\n"
                        + "}";
        private static final String FRAGMENT_SHADER =
                "precision mediump float;\n"
                        + "void main() {\n"
                        + " gl_FragColor = vec4(0.5, 0, 0, 1);\n"
                        + "}";
        private static final float[] VERTEX = {   // in counterclockwise order:
                0, 1, 0,  // top
                -0.5f, -1, 0,  // bottom left
                1, -1, 0,  // bottom right
        };

        private final FloatBuffer mVertexBuffer;

        private int mProgram;
        private int mPositionHandle;
        private int mMatrixHandle;
        private float[] mMVPMatrix = new float[16];

        MyRenderer() {
            mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(VERTEX);
            mVertexBuffer.position(0);
        }

        static int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }

        // surface 创建时回调, 做初始化工作
        // 只回调一次
        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            // OpenGL 需要加载 GLSL程序, 让GPU进行绘制

            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

            // 创建GLSL 程序
            mProgram = GLES20.glCreateProgram();

            // 加载shader代码
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

            // attach shader代码
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);

            // 链接 GLSL程序
            GLES20.glLinkProgram(mProgram);
            // 使用 GLSL程序
            GLES20.glUseProgram(mProgram);

            // 获取 shader代码中的变量索引
            // 该索引在 GLSL 程序生命周期中, 链接之后和销毁之前,  都是固定的,  只需要获取一次
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

            // 启用 vertex
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            // 绑定 vertex 坐标值
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                    12, mVertexBuffer);
        }

        // surface尺寸变化时被调用
        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            // 设置 screen space 大小
            GLES20.glViewport(0, 0, width, height);

            Matrix.perspectiveM(mMVPMatrix, 0, 45, ((float) width) / height, 0.1f, 100f);
            Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -2.5f);
        }

        // 绘制每一帧回调
        @Override
        public void onDrawFrame(GL10 unused) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        }
    }

    private static class MyRenderer1 implements GLSurfaceView.Renderer {

        private static final String VERTEX_SHADER =
                "attribute vec4 vPosition;\n"
                        + "uniform mat4 uMVPMatrix;\n"
                        + "void main() {\n"
                        + " gl_Position = uMVPMatrix * vPosition;\n"
                        + "}";
        private static final String FRAGMENT_SHADER =
                "precision mediump float;\n"
                        + "void main() {\n"
                        + " gl_FragColor = vec4(0.5, 0, 0, 1);\n"
                        + "}";
        private static final float[] VERTEX = {   // in counterclockwise order:
                1, 1, 0,   // top right
                -1, 1, 0,  // top left
                -1, -1, 0, // bottom left
                1, -1, 0,  // bottom right
        };

        private static final short[] VERTEX_INDEX = {0, 1, 2, 0, 2, 3};

        private final FloatBuffer mVertexBuffer;
        private final ShortBuffer mVertexIndexBuffer;

        private int mProgram;
        private int mPositionHandle;
        private int mMatrixHandle;
        private float[] mMVPMatrix = new float[16];

        MyRenderer1() {
            mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(VERTEX);
            mVertexBuffer.position(0);

            mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.length * 2)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer()
                    .put(VERTEX_INDEX);
            mVertexIndexBuffer.position(0);
        }

        static int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }

        // surface 创建时回调, 做初始化工作
        // 只回调一次
        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            // OpenGL 需要加载 GLSL程序, 让GPU进行绘制

            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

            // 创建GLSL 程序
            mProgram = GLES20.glCreateProgram();

            // 加载shader代码
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

            // attach shader代码
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);

            // 链接 GLSL程序
            GLES20.glLinkProgram(mProgram);
            // 使用 GLSL程序
            GLES20.glUseProgram(mProgram);

            // 获取 shader代码中的变量索引
            // 该索引在 GLSL 程序生命周期中, 链接之后和销毁之前,  都是固定的,  只需要获取一次
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

            // 启用 vertex
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            // 绑定 vertex 坐标值
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                    12, mVertexBuffer);
        }

        // surface尺寸变化时被调用
        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            // 设置 screen space 大小
            GLES20.glViewport(0, 0, width, height);

            Matrix.perspectiveM(mMVPMatrix, 0, 45, ((float) width) / height, 0.1f, 100f);
            Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -2.5f);
        }

        // 绘制每一帧回调
        @Override
        public void onDrawFrame(GL10 unused) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX.length,
                    GLES20.GL_UNSIGNED_SHORT, mVertexIndexBuffer);
        }
    }

    private static class PicRenderer implements GLSurfaceView.Renderer {

        private static final String VERTEX_SHADER =
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "attribute vec2 a_texCoord;" +
                        "varying vec2 v_texCoord;" +
                        "void main() {" +
                        " gl_Position = uMVPMatrix * vPosition;" +
                        " v_texCoord = a_texCoord;" +
                        "}";
        private static final String FRAGMENT_SHADER =
                "precision mediump float;" +
                        "varying vec2 v_texCoord;" +
                        "uniform sampler2D s_texture;" +
                        "void main() {" +
                        " gl_FragColor = texture2D(s_texture, v_texCoord);" +
                        "}";
        private static final float[] VERTEX = {   // in counterclockwise order:
                1, 1, 0,   // top right
                -1, 1, 0,  // top left
                -1, -1, 0, // bottom left
                1, -1, 0,  // bottom right
        };
        private static final float[] TEX_VERTEX = {   // in clockwise order:
                1, 0,  // bottom right
                0, 0,  // bottom left
                0, 1,  // top left
                1, 1,  // top right
        };

        private static final short[] VERTEX_INDEX = {0, 1, 2, 0, 2, 3};

        private final FloatBuffer mVertexBuffer;
        private final FloatBuffer mTexVertexBuffer;
        private final ShortBuffer mVertexIndexBuffer;
        private final Context mContext;

        private int mProgram;
        private int mPositionHandle;
        private int mMatrixHandle;
        private float[] mMVPMatrix = new float[16];
        private int mTexName;
        private int mTexCoordHandle;
        private int mTexSamplerHandle;

        PicRenderer(Context context) {
            mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(VERTEX);
            mVertexBuffer.position(0);

            mTexVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(TEX_VERTEX);
            mTexVertexBuffer.position(0);

            mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.length * 2)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer()
                    .put(VERTEX_INDEX);
            mVertexIndexBuffer.position(0);
            mContext = context;
        }

        static int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }

        // surface 创建时回调, 做初始化工作
        // 只回调一次
        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            // OpenGL 需要加载 GLSL程序, 让GPU进行绘制

            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

            // 创建GLSL 程序
            mProgram = GLES20.glCreateProgram();

            // 加载shader代码
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

            // attach shader代码
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);

            // 链接 GLSL程序
            GLES20.glLinkProgram(mProgram);
            // 使用 GLSL程序
            GLES20.glUseProgram(mProgram);

            // 获取 shader代码中的变量索引
            // 该索引在 GLSL 程序生命周期中, 链接之后和销毁之前,  都是固定的,  只需要获取一次
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

//            // 启用 vertex
//            GLES20.glEnableVertexAttribArray(mPositionHandle);
//            // 绑定 vertex 坐标值
//            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
//                    12, mVertexBuffer);


            int[] texNames = new int[1];
            GLES20.glGenTextures(1, texNames, 0);
            mTexName = texNames[0];
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.batman);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexName);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_REPEAT);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
            mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");

            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                    12, mVertexBuffer);

            GLES20.glEnableVertexAttribArray(mTexCoordHandle);
            GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0,
                    mTexVertexBuffer);
        }

        // surface尺寸变化时被调用
        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            // 设置 screen space 大小
            GLES20.glViewport(0, 0, width, height);

            Matrix.perspectiveM(mMVPMatrix, 0, 45, ((float) width) / height, 0.1f, 100f);
            Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -2.5f);
        }

        // 绘制每一帧回调
        @Override
        public void onDrawFrame(GL10 unused) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniform1i(mTexSamplerHandle, 0);

            // 用 glDrawElements 来绘制，mVertexIndexBuffer 指定了顶点绘制顺序
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX.length,
                    GLES20.GL_UNSIGNED_SHORT, mVertexIndexBuffer);
        }
    }
}
