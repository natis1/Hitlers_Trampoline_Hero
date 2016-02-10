import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;

public class Sprite extends Collidable
{
	//Whether or not the sprite is visible
	protected boolean vis;
	protected boolean keepLoaded;
	//Image of the sprite
	protected BufferedImage image;
	protected double angle = 0.0;
	//Reference to the ship image file
	protected String image_file;

	private boolean preloaded = false;

	//Constructor
	public Sprite(int x, int y, double angle, String image_file)
	{
		super(x, y);
		preloaded = false;

		this.x = x;
		this.y = y;
		this.angle = angle;
		this.image_file = image_file;

    	//when compiling for real, comment this
    	//TODO: UNCOMMENT WHEN RUNNING IN SIM
		//this.image_file = "Images\\" + image_file;

		vis = true;
	}

	public Sprite(int x, int y, double angle, BufferedImage image)
	{
		super(x, y);
		preloaded = true;

		this.x = x;
		this.y = y;
		this.angle = angle;
		this.image = createTransformedImage(image, angle);
	}

	//Load a buffered image
	public void loadImage()
	{
		if (!preloaded){
			BufferedImage img = null;
			try {
				img = ImageIO.read(getClass().getResource(image_file));
			} catch (IOException e) {
				System.out.println("Error loading image.");
				e.printStackTrace();
			}
			this.image = createTransformedImage(img, angle);
		} else {
			//You shouldn't call me ??
			StackTraceElement[] e = new Throwable().getStackTrace();
			for (int f = e.length - 1; f > 0; f--){
				System.out.println(e[f]);
			}
		}

        setSize(image.getWidth(), image.getHeight());
	}

	//Load a rotated buffered image
	public BufferedImage createTransformedImage(BufferedImage image, double angle) {
		double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
		int w = image.getWidth(), h = image.getHeight();
		int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);
		BufferedImage result = new BufferedImage(neww, newh, Transparency.TRANSLUCENT);
		Graphics2D g2d = result.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.translate((neww - w) / 2, (newh - h) / 2);
		g2d.rotate(angle, w / 2, h / 2);
		g2d.drawRenderedImage(image, null);
		g2d.dispose();
		return result;
	}

	//Set / update image dimensions
	protected void getImageDimensions() {
		width = image.getWidth(null);
		height = image.getHeight(null);
	}    

	public BufferedImage getImage() {
		return image;
	}

	public Rectangle getBoundingRectangle() {
		return new Rectangle(x, y, width, height);
	}
	
	public int getImageWidth() {
		return image.getWidth();
	}
	
	public int getImageHeight() {
		return image.getHeight();
	}

	//Center the sprite on the given x and y coordinates.
	public void moveToCenter(int x, int y){
		this.x = x - width/2;
		this.y = y - height/2;
	}

	public boolean isVisible() {
		return vis;
	}

	public void setVisible(Boolean visible) {
		vis = visible;
	}


    /* According to this link
     * http://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
     * This is the way to clone a buffered image.
     */
    private BufferedImage deepCopy(BufferedImage bi)
    {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        //return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null).getSubimage(0, 0, bi.getWidth(), bi.getHeight());
    }

    /* There is a faster way to do the following, but this worked for me and I had trouble getting
     * the faster way working.
     * http://stackoverflow.com/questions/2825837/java-how-to-do-fast-copy-of-a-bufferedimages-pixels-unit-test-included
     */
    private BufferedImage copySrcIntoDstAt(
            BufferedImage src,
            BufferedImage dst,
            int dx, int dy)
    {
        for (int x = 0; x < src.getWidth(); x++)
        {
            for (int y = 0; y < src.getHeight(); y++)
            {
                dst.setRGB( dx + x, dy + y, src.getRGB(x,y) );
            }
        }
        return dst;
    }

	public void changeAngle(double angle){
		//Remember the current center
		int[] center = getCenter();
		//Rotate the angle by pi/16
		this.angle += angle;
		//Reload the image.
		loadImage();
		//Update the image dimensions.
		getImageDimensions();
		//Move image to the old center to prevent rotation wobble.
		moveToCenter(center[0], center[1]);
	}	

	public void setAngle(double angle){
		//Remember the current center
		int[] center = getCenter();
		//Rotate the angle by pi/16
		this.angle = angle;
		//Reload the image.
		loadImage();
		//Update the image dimensions.
		getImageDimensions();
		//Move image to the old center to prevent rotation wobble.
		moveToCenter(center[0], center[1]);
	}
	
	//Check circular collision
	/*public boolean CollidedRadius(Sprite other){
		int[] my_center = this.getCenter();
		int[] other_center = other.getCenter();
		double distance = Math.sqrt((my_center[0]-other_center[0])^2 - (my_center[1]-other_center[1])^2);
		return distance < (this.collision_radius + other.collision_radius);
	}

	//Check rectangular collision
	public boolean CollidedRectangle(Sprite other){
    	//Get the rectangle around the sprite.
        Rectangle my_rect = this.getBoundingRectangle();
        Rectangle other_rect = other.getBoundingRectangle();
        return my_rect.intersects(other_rect);
	}*/
}