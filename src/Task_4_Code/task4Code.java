package Task_4_Code;

import java.util.Scanner;
 
class task4Code {
    static int n;
    static int pro[] = new int[100];
    static int sta[] = new int[100];
    static int co;
     
    public static void main(String args[])
    {
        System.out.print("Enter the number of processes: ");
        Scanner in = new Scanner(System.in);
        n = in.nextInt();
         
        int i;
         
        for(i=0;i<n;i++)
        {
            System.out.println("For process "+(i+1)+"...");
            System.out.print("Status (1 for alive, 0 for dead): ");
            sta[i]=in.nextInt();
            System.out.print("Process id (1, 2, 3, .., n): ");
            pro[i] = in.nextInt();
        }
         
        System.out.print("Which process will initiate election? ");
        int ele = in.nextInt();
         
        elect(ele);
        System.out.println("Final coordinator is "+co);
        for (int j = co - 1; j >= 1; j--) {
            if (sta[j - 1] == 1) {
                System.out.println("Process " + co + " sends coordinator message to " + j);
            }
        }
    }
    static void elect(int ele)
    {

        int recipient = ele;
        boolean aliveFlag = false;

        while (recipient < n){
            recipient+=1;
            System.out.println("Process " + ele + " sends an election message to " + recipient);
            if(sta[recipient-1] == 1){
                System.out.println(recipient + " responds to " + ele);
                aliveFlag = true;
            }
        }

        if (aliveFlag){
            elect(ele+1);
        }
        else{
            co = ele;
        }

    }
}
