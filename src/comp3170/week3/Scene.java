package comp3170.week3;

public class Scene {
	
	private Plane plane;
	
	public Scene () {
		plane = new Plane();
	}
	
	public void draw() {
		plane.draw();
	}
	
	public void update(float deltaTime) {
		plane.update(deltaTime);
	}
}
