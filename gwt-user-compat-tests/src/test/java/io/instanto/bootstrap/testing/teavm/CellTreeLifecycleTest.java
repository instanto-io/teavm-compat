package io.instanto.bootstrap.testing.teavm;

import static org.junit.Assert.*;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.cellview.client.TreeViewModel;
import com.google.gwt.view.client.ListDataProvider;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.junit.SkipJVM;
import org.teavm.junit.TeaVMTestRunner;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class CellTreeLifecycleTest {
  private static class Model implements TreeViewModel {
    final ListDataProvider<String> root = new ListDataProvider<>(Arrays.asList("branch"));
    final ListDataProvider<String> branch = new ListDataProvider<>(Arrays.asList("nested"));
    final ListDataProvider<String> nested = new ListDataProvider<>(Arrays.asList("leaf"));

    public <T> NodeInfo<?> getNodeInfo(T value) {
      return new DefaultNodeInfo<>(
          value == null ? root : "branch".equals(value) ? branch : nested, new TextCell());
    }

    public boolean isLeaf(Object value) {
      return "leaf".equals(value);
    }
  }

  @Test
  public void closingInvalidatesHandlesAndDetachesDescendants() {
    Model model = new Model();
    TreeNode root = new CellTree(model, null).getRootTreeNode();
    TreeNode branch = root.setChildOpen(0, true);
    TreeNode nested = branch.setChildOpen(0, true);
    assertNull(root.setChildOpen(0, false));
    assertTrue(branch.isDestroyed());
    assertTrue(nested.isDestroyed());
    assertTrue(model.branch.getDataDisplays().isEmpty());
    assertTrue(model.nested.getDataDisplays().isEmpty());
    try {
      branch.getChildCount();
      fail("Stale handles must reject access");
    } catch (IllegalStateException expected) {
    }
    TreeNode reopened = root.setChildOpen(0, true);
    assertNotSame(branch, reopened);
    assertFalse(reopened.isDestroyed());
    assertEquals(1, reopened.getChildCount());
  }

  @Test
  public void replacingRowsInvalidatesOldHandles() {
    Model model = new Model();
    TreeNode root = new CellTree(model, null).getRootTreeNode();
    TreeNode branch = root.setChildOpen(0, true);
    model.root.setList(Arrays.asList("replacement"));
    assertTrue(branch.isDestroyed());
    assertTrue(model.branch.getDataDisplays().isEmpty());
    assertEquals("replacement", root.getChildValue(0));
  }
}
