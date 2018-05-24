import java.util.Collection;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Optional;

abstract class Habitat{
	
	protected abstract void start(Class lifeformClass);
	
	protected Lifeform newInstance(Collection genome, Class lifeformClass){
		try {
			Constructor ct = lifeformClass.getConstructors()[0];//lifeform has only one constructor
			Object[] argslist=new Object[]{genome};
			return(Lifeform)ct.newInstance(argslist);
		}catch(Exception e){
			System.out.println("error "+e);
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	
}

class RobustHabitat extends Habitat{
	final int saveinterval = 15;
	final int habitatsize = 5;
	Class lifeformClass;
	
	protected void start(Class lifeformClass){
		this.lifeformClass=lifeformClass;
		
		final ArrayList<Collection> gencopystore = new ArrayList<Collection>();
		final Lifeform mutator = newInstance(null, lifeformClass);
		long secs = System.currentTimeMillis()/1000;
		
		for(int i=0; i<habitatsize; i++){
			gencopystore.add(mutator.newGenome());
		}

		while(true) {
			
			Optional<Collection> winner = gencopystore.stream().reduce(this::comparator);
			gencopystore.remove(winner.get());
			Optional<Collection> second = gencopystore.stream().reduce(this::comparator);
			gencopystore.clear();
			gencopystore.add(winner.get());

			for(int i=0; i<habitatsize-1; i++){
				Collection c = mutator.mutate(winner.get(), second.get());
				if(c == null){
					gencopystore.add(mutator.mutate(winner.get()));
				} else {
				    gencopystore.add(c);	
				}
			}
			
			if(System.currentTimeMillis()/1000>secs+saveinterval) {
				secs = System.currentTimeMillis()/1000;
				Lifeform lifeform = newInstance(gencopystore.get(0), lifeformClass);
				lifeform.run();
				lifeform.output();
				System.out.println("--------------------");
			}
		}
	}
	
	Collection comparator(Collection genea, Collection geneb){
		Lifeform lifeforma = newInstance(genea, lifeformClass);
		Lifeform lifeformb = newInstance(geneb, lifeformClass);

		lifeforma.run();
		lifeformb.run();
		
		if(lifeforma.getScore() > lifeformb.getScore()) {
			return genea;
		}
		return geneb;
	}
	
}
