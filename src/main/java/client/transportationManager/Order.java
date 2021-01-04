package client.transportationManager;

class Order {
	int id;
	String type;
	String startPoint;
	String endPoint;
	int weight;
	Order(int id,String type, String startPoint, String endPoint, int weight){
		this.id = id;
		this.type = type;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.weight = weight;
	}
}
