package setforge;

class LinkedSetTest extends AbstractSetTest {

    @Override
    protected SetInterface<Object> newSet() {
        return new LinkedSet<>();
    }
}
