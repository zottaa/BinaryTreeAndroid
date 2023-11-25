package com.github.zottaa.binarytree;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.*;

public interface Serialize {
    public void serialize(BinaryTree tree, Uri uri, String type, ContentResolver contentResolver);

    public BinaryTree deserialize(Uri uri, ContentResolver contentResolver);

    abstract class Abstract implements Serialize {
        public void serialize(BinaryTree tree, Uri uri, String type, ContentResolver contentResolver) {
            try (OutputStream outputStream = contentResolver.openOutputStream(uri);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {

                writer.write(type);
                writer.newLine();

                tree.forEachFromRoot(new ElementProcessor<UserType>() {
                    @Override
                    public void toDo(UserType v) {
                        try {
                            writer.write(" ");
                            writer.write(v.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public BinaryTree deserialize(Uri uri, ContentResolver contentResolver) {
            try (InputStream inputStream = contentResolver.openInputStream(uri);
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {

                String type = bufferedReader.readLine();
                UserFactory userFactory = new UserFactory();
                if (!userFactory.getTypeNameList().contains(type)) {
                    throw new IllegalArgumentException("Wrong type");
                }

                String line;
                BinaryTree tree = new BinaryTree.Base();
                while ((line = bufferedReader.readLine()) != null) {
                    String[] items = line.split(" ");

                    for (String item : items) {
                        UserType builder = userFactory.getBuilderByName(type);
                        Object object = builder.parseValue(item);
                        if (object != null) {
                            tree.add((UserType) object);
                        }
                    }
                }
                return tree;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    class Base extends Abstract {

    }
}
