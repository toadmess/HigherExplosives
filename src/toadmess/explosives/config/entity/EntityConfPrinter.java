package toadmess.explosives.config.entity;

import java.util.List;

import toadmess.explosives.config.ConfProps;

/**
 * Represents a single entity's configuration within a single world.
 * 
 * @author John Revill
 */
public class EntityConfPrinter {
	private final EntityConf toPrint;

	public EntityConfPrinter(final EntityConf toPrint) {		
		this.toPrint = toPrint;
	}

	public String stringify() {
		String str = ""; 
		
		str += propToString(ConfProps.CONF_BOUNDS);
		
		str += multipliersToString(ConfProps.CONF_ENTITY_RADIUSMULT);
		str += multipliersToString(ConfProps.CONF_ENTITY_PLAYER_DAMAGEMULT);
		str += multipliersToString(ConfProps.CONF_ENTITY_CREATURE_DAMAGEMULT);
		str += multipliersToString(ConfProps.CONF_ENTITY_ITEM_DAMAGEMULT);
		str += multipliersToString(ConfProps.CONF_ENTITY_TNT_FUSEMULT);
		
		str += propToString(ConfProps.CONF_ENTITY_PREVENT_TERRAIN_DAMAGE);
		str += propToString(ConfProps.CONF_ENTITY_FIRE);
		str += propToString(ConfProps.CONF_ENTITY_YIELD);
		
		str += specificYieldsToString();
		
		str += propToString(ConfProps.CONF_ENTITY_TNT_TRIGGER_PREVENTED);
		
		str += subConfigToString(ConfProps.CONF_ENTITY_TNT_TRIGGER_HAND);
		str += subConfigToString(ConfProps.CONF_ENTITY_TNT_TRIGGER_FIRE);
		str += subConfigToString(ConfProps.CONF_ENTITY_TNT_TRIGGER_REDSTONE);
		str += subConfigToString(ConfProps.CONF_ENTITY_TNT_TRIGGER_EXPLOSION);
		str += subConfigToString(ConfProps.CONF_ENTITY_CREEPER_CHARGED);
		
		str += subConfigListToString(ConfProps.CONF_PERMISSIONS_LIST);
		
		return "Conf("+permissionNamesToString()+"\n" + indent(str) + "\n)";
	}

	private String indent(final String lines) {
		String indented = "";
		
		for(final String line : lines.split("\n")) {
			indented += indented.equals("") ? "" : "\n" ;
			indented += "  " + line;
		}
		
		return indented;
	}
	
	private String permissionNamesToString() {
		final String permName = this.toPrint.getPermissionNodeName();
		final String groupName = this.toPrint.getPermissionGroupName();
		
		String appliesTo = "";
		if(permName != null) {
			appliesTo += "permission \"" + permName + "\"";
		}
		if(groupName != null) {
			if(permName != null) appliesTo += " and "; 
			appliesTo += "group \"" + groupName + "\"";
		}
		if(!"".equals(appliesTo)) {
			appliesTo = " [for " + appliesTo + "]";
		}
		return appliesTo;
	}

	private String subConfigListToString(final ConfProps subConfListProperty) {
		@SuppressWarnings("unchecked")
		final List<EntityConf> subConfs = (List<EntityConf>) toPrint.getOwnProp(subConfListProperty);
		
		if(subConfs == null) {
			return "";
		}
		
		String str = "";
		
		for(final EntityConf ec : subConfs) {
			if("".equals(str)) {
				str += ec.toString();				
			} else {
				str += ",\n" + ec.toString().replace("inherited", "NB: inherits from permissions configs above if player also has those extra permissions");
			}
			
		}
		
		return subConfListProperty.toString() + "={\n" + this.indent(str) + ",\n";
	}
	
	private String subConfigToString(final ConfProps subConfProperty) {
		final EntityConf subConf = (EntityConf) toPrint.getOwnProp(subConfProperty);
		
		String str = "";
		
		if(toPrint.isSubConfig()) {
			// Some sub-config properties don't make sense themselves in sub configs, such as these..
			switch(subConfProperty) {
			case CONF_ENTITY_TNT_TRIGGER_HAND:
			case CONF_ENTITY_TNT_TRIGGER_FIRE:
			case CONF_ENTITY_TNT_TRIGGER_REDSTONE:
			case CONF_ENTITY_TNT_TRIGGER_EXPLOSION:
			case CONF_ENTITY_CREEPER_CHARGED:
				return "";
			default:
				// Carry on..
			}
		}
		
		if(subConf == null) {
			str += "no sub-configuration specified";
		} else {
			str += subConf.toString();
		}
		
		return subConfProperty.toString() + "=" + str + ",\n";
	}
	
	private String propToString(final ConfProps confProperty) {
		String str = "";
		
		final Object prop = toPrint.getInheritedProp(confProperty);
		
		if(prop == null) {
			if(toPrint.isSubConfig()) {
				return "";
			} else {
				str += "not configured, will be left unaffected";				
			}
		} else if(!toPrint.hasOwnProp(confProperty)) {
			str += "inherited";
		} else {			
			str += prop;
		}
		
		return confProperty.toString() + "=" + str + ",\n";
	}
	
	private String multipliersToString(final ConfProps multiplierProperty) {
		String str = "";
		
		@SuppressWarnings("unchecked")
		final List<List<Float>> paramList = (List<List<Float>>) toPrint.getInheritedProp(multiplierProperty);
		if(paramList == null) {
			if(toPrint.isSubConfig()) {
				return ""; // For conciseness, don't bother showing unspecified properties for sub configs
			} else {
				str += "no multiplier configured. will leave unaffected";
			}
		} else if(!toPrint.hasOwnProp(multiplierProperty)) {
			str += "inherited";
		} else {
			str += "\n";
			
			for (final List<?> localList : paramList) {
				str = str + "(chance:" + localList.get(0) + ", value:" + localList.get(1) + ")\n";
			}
			str = "{" + indent(str) + "\n}";
		}

		return multiplierProperty + "=" + str + ",\n";
	}

	private String specificYieldsToString() {
		final Float[] specificYields = toPrint.getSpecificYieldConfig();
		
		String str = "";
		
		if(specificYields == null) {
			if(toPrint.isSubConfig()) {
				return "";
			}
			str += "no specific block yields configured";
		} else if(!toPrint.hasOwnProp(ConfProps.CONF_ENTITY_YIELD_SPECIFIC)) {
			str += "inherited";
		} else {
			for (int i = 0; i < specificYields.length; i++) {
				if(specificYields[i] != null) {
					str += "(block ID " + i + " has yield " + specificYields[i] + ")\n";
				}
			}
			str = "{\n" + indent(str) + "\n}";
		}
		
		return "yieldSpecific=" + str + ",\n";
	}
}