package toadmess.explosives.config;

public enum ConfProps {
	CONF_VERSION("version"),
	
	CONF_DEBUGCONFIG("debugConfig"),
	
	CONF_ENTITIES("entities"),
	CONF_ENTITY_FIRE("fire"),

	CONF_ENTITY_RADIUSMULT("radiusMultiplier"),
	CONF_ENTITY_PLAYER_DAMAGEMULT("playerDamageMultiplier"),
	CONF_ENTITY_CREATURE_DAMAGEMULT("creatureDamageMultiplier"),
	CONF_ENTITY_ITEM_DAMAGEMULT("itemDamageMultiplier"),
	CONF_ENTITY_YIELD("yield"),
	CONF_ENTITY_YIELD_SPECIFIC("yieldSpecific"),
	CONF_ENTITY_PREVENT_TERRAIN_DAMAGE("preventTerrainDamage"),
	CONF_ENTITY_TNT_FUSEMULT("tntFuseMultiplier"),
		
	CONF_ENTITY_TNT_TRIGGER_REDSTONE("tntPrimeByRedstone"),
	CONF_ENTITY_TNT_TRIGGER_FIRE("tntPrimeByFire"),
	CONF_ENTITY_TNT_TRIGGER_HAND("tntPrimeByHand"),
	CONF_ENTITY_TNT_TRIGGER_EXPLOSION("tntPrimeByExplosion"),
		
	CONF_ENTITY_TNT_TRIGGER_PREVENTED("tntPrimePrevented"),
		
	CONF_MULTIPLIER_CHANCE("chance"),
	CONF_MULTIPLIER_VALUE("value"),
		
	CONF_BOUNDS("activeBounds"),
	CONF_BOUNDS_MAX("max"),
	CONF_BOUNDS_MIN("min"),
	
	CONF_WORLDS("worlds");
	
	private final String confPropertyName;
	@Override
	public String toString() { return confPropertyName; }
	
	ConfProps(final String confPropertyName) {
		this.confPropertyName = confPropertyName;
	}
}
