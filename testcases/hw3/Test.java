class Test {
    public static void main(String[] args) {
      System.out.println(new A().run());
    }
  }
  
  class A {
    int a;

    public int run() {
        a = 0;
        while (a < 10){
            System.out.println(a);
            a = a + 1;
        }

        return a;
    }
  }
  