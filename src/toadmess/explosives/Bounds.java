package toadmess.explosives;

import org.bukkit.Location;
import org.bukkit.util.config.Configuration;

/**
 * Simple class to check 
 *
 * @author John Revill
 */
public class Bounds {
	private final Double allowedBoundsMinX;
	private final Double allowedBoundsMaxX;

	private final Double allowedBoundsMinY;
	private final Double allowedBoundsMaxY;

	private final Double allowedBoundsMinZ;
	private final Double allowedBoundsMaxZ;

	public Bounds(final Configuration conf, final String confPath) {
		this.allowedBoundsMinX = getOptionalDouble(conf, confPath + "." + HEMain.CONF_BOUNDS_MIN + ".x");
		this.allowedBoundsMaxX = getOptionalDouble(conf, confPath + "." + HEMain.CONF_BOUNDS_MAX + ".x");
		
		this.allowedBoundsMinY = getOptionalDouble(conf, confPath + "." + HEMain.CONF_BOUNDS_MIN + ".y");
		this.allowedBoundsMaxY = getOptionalDouble(conf, confPath + "." + HEMain.CONF_BOUNDS_MAX + ".y");
		
		this.allowedBoundsMinZ = getOptionalDouble(conf, confPath + "." + HEMain.CONF_BOUNDS_MIN + ".z");
		this.allowedBoundsMaxZ = getOptionalDouble(conf, confPath + "." + HEMain.CONF_BOUNDS_MAX + ".z");
	}
	
	public boolean isWithinBounds(final Location epicentre) {
		if(null == epicentre) {			
			return false;
		}
		
		final double epicentreX = epicentre.getX();
		final double epicentreY = epicentre.getY();
		final double epicentreZ = epicentre.getZ();

		if(null != this.allowedBoundsMinX && epicentreX < this.allowedBoundsMinX) {
			return false;
		}
		if(null != this.allowedBoundsMinY && epicentreY < this.allowedBoundsMinY) {
			return false;
		}
		if(null != this.allowedBoundsMinZ && epicentreZ < this.allowedBoundsMinZ) {
			return false;
		}

		if(null != this.allowedBoundsMaxX && epicentreX > this.allowedBoundsMaxX) {
			return false;
		}
		if(null != this.allowedBoundsMaxY && epicentreY > this.allowedBoundsMaxY) {
			return false;
		}
		if(null != this.allowedBoundsMaxZ && epicentreZ > this.allowedBoundsMaxZ) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * @param conf The Configuration object to interrogate
	 * @param confPath The yaml path to the wanted optional parameter, e.g. "entities.TNTPrimed.allowedBounds.max.y"
	 * 
	 * @return Null if the configuration at the given path is not specified. If it is specified, a Float of its value is returned.
	 */
	private Double getOptionalDouble(final Configuration conf, final String confPath) {
		if(null == conf.getProperty(confPath)) {
			return null;
		}
		
		return conf.getDouble(confPath, 0.0D);
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Bounds(");
		
		if(null != allowedBoundsMinX) sb.append("minx=").append(allowedBoundsMinX);
		if(null != allowedBoundsMinY) sb.append(" miny=").append(allowedBoundsMinY);
		if(null != allowedBoundsMinZ) sb.append(" minz=").append(allowedBoundsMinZ);

		if(null != allowedBoundsMaxX) sb.append(" maxx=").append(allowedBoundsMaxX);
		if(null != allowedBoundsMaxY) sb.append(" maxy=").append(allowedBoundsMaxY);
		if(null != allowedBoundsMaxZ) sb.append(" maxz=").append(allowedBoundsMaxZ);
		
		sb.append(")");
		return sb.toString();
	}
}
