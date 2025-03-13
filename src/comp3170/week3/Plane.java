package comp3170.week3;

import static comp3170.Math.TAU;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import comp3170.GLBuffers;
import comp3170.Shader;
import comp3170.ShaderLibrary;

public class Plane {

	final private String VERTEX_SHADER = "vertex.glsl";
	final private String FRAGMENT_SHADER = "fragment.glsl";

	private Vector4f[] vertices;
	private int vertexBuffer;
	private int[] indices;
	private int indexBuffer;
	private Vector3f[] colours;
	private int colourBuffer;

	private Shader shader;
	private Matrix4f modelMatrix = new Matrix4f();
	private Matrix4f transMatrix = new Matrix4f();
	private Matrix4f rotMatrix = new Matrix4f();
	private Matrix4f scalMatrix = new Matrix4f();
	
	final private float MOVEMENT_SPEED = 4f;
	final private float SCALE_RATE = 0.1f;
	final private float ROTATION_RATE = TAU/12;
	
	public Plane() {

		shader = ShaderLibrary.instance.compileShader(VERTEX_SHADER, FRAGMENT_SHADER);

		// @formatter:off
			//          (0,1)
			//           /|\
			//          / | \
			//         /  |  \
			//        / (0,0) \
			//       /   / \   \
			//      /  /     \  \
			//     / /         \ \		
			//    //             \\
			//(-1,-1)           (1,-1)
			//
	 		
		vertices = new Vector4f[] {
			new Vector4f( 0, 0, 0, 1),
			new Vector4f( 0, 1, 0, 1),
			new Vector4f(-1,-1, 0, 1),
			new Vector4f( 1,-1, 0, 1),
		};
			
			// @formatter:on
		vertexBuffer = GLBuffers.createBuffer(vertices);

		// @formatter:off
		colours = new Vector3f[] {
			new Vector3f(1,0,1),	// MAGENTA
			new Vector3f(1,0,1),	// MAGENTA
			new Vector3f(1,0,0),	// RED
			new Vector3f(0,0,1),	// BLUE
		};
			// @formatter:on

		colourBuffer = GLBuffers.createBuffer(colours);

		// @formatter:off
		indices = new int[] {  
			0, 1, 2, // left triangle
			0, 1, 3, // right triangle
			};
			// @formatter:on

		indexBuffer = GLBuffers.createIndexBuffer(indices);
		
		vertexBuffer = GLBuffers.createBuffer(vertices);
		indexBuffer = GLBuffers.createIndexBuffer(indices);
		colourBuffer = GLBuffers.createBuffer(colours);
		
		//Default Values 
		//float translateX = 0.0f;
		//float translateY = 0.0f;
		//float scaleX = 1.0f;
		//float scaleY = 1.0f;
		//float rotation = 0.0f;
		
		//translationMatrix(translateX, translateY, transMatrix);
		//scaleMatrix(scaleX, scaleY, scalMatrix);
		//rotationMatrix(rotation, rotMatrix);
		//modelMatrix.mul(transMatrix).mul(scalMatrix).mul(rotMatrix);
		
		//Output A:
		//scaleMatrix(-1.0f, 1.0f, scalMatrix);
		//modelMatrix.mul(scalMatrix);
		
		//Output B:
		//rotationMatrix(-TAU/4, rotMatrix);
		//modelMatrix.mul(rotMatrix);
		
		//Output C:
		//modelMatrix.translation(0.5f,-0.5f,0.0f).scale(0.5f);
		//translationMatrix(0.5f,-0.5f,transMatrix);
		//scaleMatrix(0.5f,0.5f,scalMatrix);
		//modelMatrix.mul(transMatrix).mul(scalMatrix);
		
		//Output D:
		//float side = (float)(0.5f/Math.sqrt(2)); //calculates how far the shape needs to be translated based on diagonals
		//translationMatrix(-1+side,1-side,transMatrix);
		//rotationMatrix(TAU/8,rotMatrix);
		//scaleMatrix(0.1f,0.1f,scalMatrix);
		//modelMatrix.mul(transMatrix).mul(rotMatrix).mul(scalMatrix);
		
		//Object moving in circle
		modelMatrix.translate(0.0f,0.8f,0.0f).scale(SCALE_RATE).rotateZ(TAU/4);

	}

public void update(float deltaTime) {
		
		float movement = MOVEMENT_SPEED * deltaTime;
		float rotation = ROTATION_RATE * deltaTime;
		modelMatrix.translate(0.0f,movement,0.0f).rotateZ(rotation);
	}
	
	public void draw() {
		
		shader.enable();
		
		
		shader.setUniform("u_modelMatrix", modelMatrix);
		
		// set the attributes
		
		shader.setAttribute("a_position", vertexBuffer);
		shader.setAttribute("a_colour", colourBuffer);

		// draw using index buffer
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);

	}

	/**
	 * Set the destination matrix to a translation matrix. Note the destination
	 * matrix must already be allocated.
	 * 
	 * @param tx   Offset in the x direction
	 * @param ty   Offset in the y direction
	 * @param dest Destination matrix to write into
	 * @return
	 */

	public static Matrix4f translationMatrix(float tx, float ty, Matrix4f dest) {
		// clear the matrix to the identity matrix
		dest.identity();

		//     [ 1 0 0 tx ]
		// T = [ 0 1 0 ty ]
	    //     [ 0 0 0 0  ]
		//     [ 0 0 0 1  ]

		// Perform operations on only the x and y values of the T vec. 
		// Leaves the z value alone, as we are only doing 2D transformations.
		
		dest.m30(tx);
		dest.m31(ty);

		return dest;
	}

	/**
	 * Set the destination matrix to a rotation matrix. Note the destination matrix
	 * must already be allocated.
	 *
	 * @param angle Angle of rotation (in radians)
	 * @param dest  Destination matrix to write into
	 * @return
	 */

	public static Matrix4f rotationMatrix(float angle, Matrix4f dest) {

			//	   [ cos(a) -sin(a)	0 0 ]
			// R = [ sin(a) cos(a)	0 0 ]
		    //     [ 0        0   	0 0 ]
			//     [ 0 		  0 	0 1 ]
		dest.identity();
		
		dest.m00((float) Math.cos(angle));
		dest.m01((float) Math.sin(angle));
		dest.m10((float) Math.sin(-angle));
		dest.m11((float) Math.cos(angle));

		return dest;
	}

	/**
	 * Set the destination matrix to a scale matrix. Note the destination matrix
	 * must already be allocated.
	 *
	 * @param sx   Scale factor in x direction
	 * @param sy   Scale factor in y direction
	 * @param dest Destination matrix to write into
	 * @return
	 */

	public static Matrix4f scaleMatrix(float sx, float sy, Matrix4f dest) {

			//	   [ sx 0 0 0  ]
			// S = [ 0 sy 0 0  ]
		    //     [ 0  0 0 0  ]
			//     [ 0  0 0 1  ]

		dest.identity();
		
		dest.m00(sx);
		dest.m11(sy);
		
		return dest;
	}

}
