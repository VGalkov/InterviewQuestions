package ru.galkov.other;

public class Recursion {
	
	public static void main(String[] args) { 
		Recursion r = new Recursion();
		r.recurse(18);
		System.out.println();
		System.out.println(r.factorial(9));
		System.out.println(r.fibonacci(9));
	}
	
	
	
    public int factorial(int n) {
        if (n==0) return 1;
        else if (n==1) return 1;
        else return factorial(n-1)*n;

    }
	
    public int fibonacci(int n) {
        if (n==0)            return 0;
        else if (n == 1)    return 1; 
        else return fibonacci(n - 1) + fibonacci(n - 2);
    }
    
    
    
    public void recurse(int n) {
        if (n > 1) {
        for (int i = 2; i <= n; i++) {
            if (n % i == 0) {
                System.out.print(i + " ");
                n = n / i;
                recurse(n);
                return;
                }
            }
        }
    
    }
}
