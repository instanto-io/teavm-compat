package io.instanto.compat;

import static org.junit.Assert.*;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.junit.SkipJVM;
import org.teavm.junit.TeaVMTestRunner;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class WidgetCollectionContractTest {
  @Test
  public void iteratorRemovalDelegatesToTheOwnerAndContinuesWithTheNextChild() {
    Owner owner = new Owner();
    Widget first = new Label("first"), second = new Label("second");
    owner.add(first);
    owner.add(second);
    Iterator<Widget> children = owner.iterator();
    assertThrows(IllegalStateException.class, children::remove);
    assertSame(first, children.next());
    children.remove();
    assertEquals(1, owner.removals);
    assertThrows(IllegalStateException.class, children::remove);
    assertSame(second, children.next());
    assertFalse(children.hasNext());
    assertThrows(NoSuchElementException.class, children::next);
  }

  @Test
  public void childrenAreLocatedByIdentityEvenIfTheyCompareEqual() {
    Owner owner = new Owner();
    Widget first = new EqualLabel(), second = new EqualLabel();
    owner.add(first);
    owner.add(second);
    assertEquals(1, owner.children.indexOf(second));
    assertFalse(owner.children.contains(new EqualLabel()));
    owner.children.remove(second);
    assertSame(first, owner.children.get(0));
  }

  private static final class EqualLabel extends Label {
    @Override
    public boolean equals(Object other) {
      return other instanceof EqualLabel;
    }

    @Override
    public int hashCode() {
      return 1;
    }
  }

  private static final class Owner implements HasWidgets {
    final WidgetCollection children = new WidgetCollection(this);
    int removals;

    @Override
    public void add(Widget child) {
      children.add(child);
    }

    @Override
    public boolean remove(Widget child) {
      removals++;
      children.remove(child);
      return true;
    }

    @Override
    public void clear() {
      Iterator<Widget> iterator = iterator();
      while (iterator.hasNext()) {
        iterator.next();
        iterator.remove();
      }
    }

    @Override
    public Iterator<Widget> iterator() {
      return children.iterator();
    }
  }
}
