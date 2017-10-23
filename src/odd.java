import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
 
class odd {
	private int n;
 
	{
		System.out.println("Hello world!");
	}
     
	public odd() {}
	
	public odd(int n) {
		this.n = n;
		System.out.println("This is parametrized");
	}
	
	public int reverse(int no) {
		int rev = 0;
		
		while(no != 0) {
			rev = (rev * 10) + (no % 10);
			no = no / 10;
		}
		
		return rev;
	}
	
	public void insertionSort(int[] num) {
		int n = num.length;
		
		for(int j = 1; j < n ; j++) {
			int key = num[j];
			int i = j - 1;
			
			while((i > -1) && (num[i] > key)) {
				num[i + 1] = num[i];
				i--;
			}
			num[i + 1] = key;
		}
	}
	
	public void bubbleSort(int[] num) {
		int n = num.length;
		
		for(int j = n; j >= 0; j--) {
			for(int i = 0; i < n-1; i++) {
				if(num[i] > num[i+1]) {
					int temp = num[i];
					num[i] = num[i+1];
					num[i+1] = temp;
				}
			}
		}
	}
	
	public int strToInt(String num) {
		int sum = 0;
		
		char chrs[] = num.toCharArray();
		int zeroAscii = (int)'0';
		
		System.out.println("Zeroacii : "+zeroAscii);
		for(char c:chrs) {
			int tmpAscii = (int)c;
			sum = (sum *10) + (tmpAscii - zeroAscii);
		}
		
		return sum;
	}
	
	 public int printTwoMaxNumbers(int[] nums){
	        int minOne = 10000000;
	        int minTwo = 10000000;
	        for(int n:nums){
	            if(minOne > n){
	                minTwo = minOne;
	                minOne =n;
	            } else if(minTwo > n){
	                minTwo = n;
	            }
	        }
	       
	        return minTwo;
	    }
	 
	 public int tot(int a[], int k) {
		 int tot_cnt = 0;
		 
		 for(int i = 0; i < a.length; i++) {
			 for(int j = 0 ; j < a.length; k++) {
				 if(a[i] + a[j] == k) {
					 tot_cnt += 1;
				 }
			 }
		 }
		 
		 return tot_cnt;
	 }
	 
    public static void main(String a[]){
        List<Integer> numbers = new ArrayList<Integer>();
        odd o = new odd(2);
        
        
        int num[] = {1,2,3,4};
        
        
        System.out.printf("Num : %d", o.tot(num, 5));
        
        
        
        
    }
    
    
}