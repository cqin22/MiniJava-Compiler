class Test {
    public static void main(String[] a){
        System.out.println(new BT().Start());
        }
}

class BT {
    A once;
    public int Start(){
        once = new A();

        System.out.println(once.Upon());
        return 1;
    }
}

class A {
    public int Upon(){
        return 2;
    }
}
