package mainpack;



import java.util.LinkedList;

public class MyQueue <T>{
	private LinkedList<T> queue;
	private int size;
	
	public MyQueue(){
		queue = new LinkedList<T>();
		size = 0;
	}

	public void enqueue(T sth){
		queue.addLast(sth);
		size++;
	}
	
	public T dequeue(){
		size--;
		return queue.remove();
	}
	
	public T peek(){
		return queue.getFirst();
	}
	
	public T peekLast(){
		return queue.getLast();
	}
	
	public void clear(){
		queue.clear();
		size = 0;
	}
	
	public boolean isEmpty(){
		return queue.isEmpty();
	}
	
	public int size(){
		return size;
	}
}
