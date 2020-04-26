package ru.galkov.other;


public class IteratorEx {
	
	public static void main(String[] args) {
		ObjectContainer oc = new ObjectContainer();
		Iterator it = oc.getIterator();
		
		while (it.hasnext()) {
			System.out.println(it.next());
		}
	}
}


	interface Iterator {
		boolean hasnext();
		Object next();
	}
	
	
	interface Container {
		Iterator getIterator();
		
	}

	
	
	class ObjectContainer implements Container {
		String [] array = {"one", "two", "three"};

		@Override
		public Iterator getIterator() {
			
			return new ArrIterator();
		}
		
		class  ArrIterator implements Iterator{
			int index =0;
				
			@Override
			public Object next() {
				if ( hasnext()) return array[index++];
				return null;
			}

			@Override
			public boolean hasnext() {
				// TODO Auto-generated method stub
				return (index < array.length ) ? true : false;
			}
			
			
			
		}
		
		
		
}

