package lucene;

public class Pair<T, T1> implements P<T,T1>{

	public T fst;
	public T1 snd;
	public Pair(T f, T1 s){
		fst = f;
		snd = s;
	}
	@Override
	public T fst() {
		return fst;
	}
	@Override
	public T1 snd() {
		return snd;
	}
	
	
}
