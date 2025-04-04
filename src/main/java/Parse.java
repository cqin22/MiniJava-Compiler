import java.util.Scanner;

public class Parse{
  Token token_class = new Token();
  char token;
  
  public Parse(){
  }

  void error(){
    System.out.println("Parse error\n");
    System.exit(0);
  }

  void eat(char a){
    if(a == token){ 
      token = token_class.next_token(); 
    }
    else{
      error();
    }
  }

  void S(){
    String match = "";
    switch(token){
      case '{':
        eat('{');
        while(token == ' '){
          eat(' ');
        }
        L();
        while(token == ' '){
          eat(' ');
        }
        eat('}');
        break;
      case 'S':
        match = "System.out.println(";
        for(int i = 0; i < match.length(); i++){
          eat(match.charAt(i));
        }
        while(token == ' '){
          eat(' ');
        }
        E();
        while(token == ' '){
          eat(' ');
        }
        eat(')');
        eat(';');
        break;
      case 'i':
        eat('i');
        eat('f');
        while(token == ' '){
          eat(' ');
        }
        eat('(');
        while(token == ' '){
          eat(' ');
        }
        E();
        while(token == ' '){
          eat(' ');
        }
        eat(')');
        while(token == ' '){
          eat(' ');
        }
        S();
        while(token == ' '){
          eat(' ');
        }
        eat('e');
        eat('l');
        eat('s');
        eat('e');
        while(token == ' '){
          eat(' ');
        }
        S();
        break;
      case 'w':
        match = "while";
        for(int i = 0; i < match.length(); i++){
          eat(match.charAt(i));
        }
        while(token == ' '){
          eat(' ');
        }
        eat('(');
        while(token == ' '){
          eat(' ');
        }
        E();
        while(token == ' '){
          eat(' ');
        }
        eat(')');
        while(token == ' '){
          eat(' ');
        }
        S();
        break;
      default:
        error();
    }
    
  }

  void L(){
    switch(token){
      case '{':
      case 'S':
      case 'i':
      case 'w':
        while(token == ' '){
          eat(' ');
        }
        S();
        while(token == ' '){
          eat(' ');
        }
        L();
        while(token == ' '){
          eat(' ');
        }
      break;
      default:
    }
  }

  void E(){
    String match = "";
    switch(token){
      case 't':
        match = "true";
        for(int i = 0; i < match.length(); i++){
          eat(match.charAt(i));
        }
        break;
      case 'f':
        match = "false";
        for(int i = 0; i < match.length(); i++){
          eat(match.charAt(i));
        }
        break;
      case '!':
        eat('!');
        while(token == ' '){
          eat(' ');
        }
        E();
        break;
    }
  }

  public static void main(String [] args) {
    Parse p = new Parse();
    p.token = p.token_class.next_token();
    p.S();
    if(p.token_class.index < p.token_class.input.length()){
      p.error();
    }
    System.out.println("Program parsed successfully\n");
  }
}

class Token{
  String input = "";
  int index = 0;
  public Token(){
    Scanner scanner = new Scanner(System.in);
    while(scanner.hasNextLine()){
      String line = scanner.nextLine();
      input += line;
    }
    input += " ";
    // System.out.println(input);
  }

  char next_token(){
    char out = input.charAt(index);
    index ++;
    return out;
  }
}
