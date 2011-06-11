package toadmess.explosives;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import toadmess.explosives.config.ConfConstants;

/**
 * Simple class to check 
 *
 * @author John Revill
 */
public class Bounds implements ConfConstants {
	private final Vector minVector;
	private final Vector maxVector;
	
	public Bounds(final Configuration conf, final String confPathPrefix) {
		final String confPathMin = confPathPrefix + "." + CONF_BOUNDS + "." + CONF_BOUNDS_MIN;
		final String confPathMax = confPathPrefix + "." + CONF_BOUNDS + "." + CONF_BOUNDS_MAX;

		this.minVector = new Vector(conf.getDouble(confPathMin + ".x", Double.NEGATIVE_INFINITY), 
									conf.getDouble(confPathMin + ".y", Double.NEGATIVE_INFINITY), 
									conf.getDouble(confPathMin + ".z", Double.NEGATIVE_INFINITY));
		
		this.maxVector = new Vector(conf.getDouble(confPathMax + ".x", Double.POSITIVE_INFINITY), 
									conf.getDouble(confPathMax + ".y", Double.POSITIVE_INFINITY), 
									conf.getDouble(confPathMax + ".z", Double.POSITIVE_INFINITY));
	}
	
	public boolean isWithinBounds(final Location epicentre) {
		if(null == epicentre) {			
			return false;
		}
		
		return epicentre.toVector().isInAABB(this.minVector, this.maxVector);
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Bounds(");
		
		if(!Double.isInfinite(minVector.getX())) sb.append("minx=").append(minVector.getX());
		if(!Double.isInfinite(minVector.getY())) sb.append(" miny=").append(minVector.getY());
		if(!Double.isInfinite(minVector.getZ())) sb.append(" minz=").append(minVector.getZ());

		if(!Double.isInfinite(maxVector.getX())) sb.append(" maxx=").append(maxVector.getX());
		if(!Double.isInfinite(maxVector.getY())) sb.append(" maxy=").append(maxVector.getY());
		if(!Double.isInfinite(maxVector.getZ())) sb.append(" maxz=").append(maxVector.getZ());
		
		sb.append(")");
		return sb.toString();
	}

	// Getters used in unit tests
	public Double getMinX() { return Double.isInfinite(minVector.getX()) ? null : minVector.getX(); }
	public Double getMinY() { return Double.isInfinite(minVector.getY()) ? null : minVector.getY(); }
	public Double getMinZ() { return Double.isInfinite(minVector.getZ()) ? null : minVector.getZ(); }
	
	public Double getMaxX() { return Double.isInfinite(maxVector.getX()) ? null : maxVector.getX(); }
	public Double getMaxY() { return Double.isInfinite(maxVector.getY()) ? null : maxVector.getY(); }
	public Double getMaxZ() { return Double.isInfinite(maxVector.getZ()) ? null : maxVector.getZ(); }
}
