class Test {
    public static void main(String[] a){
        System.out.println(new BT().Start());
        }
}

class BT {
    A once;
    boolean a;
    // what if int b is here
    public boolean Start(){
        boolean b;
        b = true;
        a = false;
        if(b && !a)
        {
            System.out.println(9);
        }
        else{   
            System.out.println(8);

        }

        return false;
    }

    public int End(){
        return 9;
    }

}

class A {
    public boolean Upon(boolean a){
        return a;
    }
}
