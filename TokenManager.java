package assignment1;
import java.util.LinkedList;
import java.util.Optional;

/**manages token stream */
public class TokenManager {

    private LinkedList<Token> tokenStream;

    /**constructor that accepts linked list and sets the private member tokenStream*/
    public TokenManager(LinkedList<Token> stream){
        this.tokenStream = stream;
    }

    /**Peeks j tokens ahead and returns the token if we aren't past the end of token list */
    public Optional<Token> Peek(int j){
        int remaining = tokenStream.size();
        if (j >= remaining)
            return Optional.empty();
        return Optional.of(tokenStream.get(j));
    }

    /**returns true if the token list is not empty */
    public Boolean MoreTokens(){
        return !tokenStream.isEmpty();
    }

    /**looks at the head of the list and if it's the same as what we passed,
     * removes token and returns. If not, returns optional.empty()*/
    public Optional<Token> MatchAndRemove(Token.tokenType t){
        Optional<Token> head = Peek(0);
        if(head.isPresent()){
            if(head.get().gettype().equals(t)){
               return Optional.of(tokenStream.remove());
            }
        }
        return Optional.empty();
    }


}
