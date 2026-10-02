package setforge;

class ResizableArraySetTest extends AbstractSetTest {

    @Override
    protected SetInterface<Object> newSet() {
        return new ResizableArraySet<>();
    }
}
