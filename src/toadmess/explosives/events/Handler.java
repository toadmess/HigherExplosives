package toadmess.explosives.events;

public interface Handler {
	void handle(final HEEvent event);
	
	TippingPoint[] getTippingPointsHandled();
}
