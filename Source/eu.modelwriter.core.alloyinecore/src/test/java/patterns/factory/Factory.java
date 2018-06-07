/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017, Ferhat Erata <ferhat@computer.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package patterns.factory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class Factory {

    public static <T extends Element> T create(java.lang.Class<T> type) {
        if (type.isAssignableFrom(CommentImp.class))
            return type.cast(new CommentImp());

        else return null;
    }


    public static Comment createComment(){
        return new CommentImp();
    }

    private static class ElementImp implements Element{

        private Set<Element> ownedElement = new LinkedHashSet<>();
        private Element owner;

        @Override
        public List<Comment> ownedComment() { return ownedElement(Comment.class); }

        @Override
        public List<Element> ownedElement() { return ownedElement.stream().collect(Collectors.toList()); }

        @Override
        public Element owner() { return owner; }
    }

    private static class CommentImp implements Comment {

        private String body;
        private Element element = new ElementImp();
        private Set<Element> annotatedElement = new LinkedHashSet<>();

        @Override
        public String body() { return body; }

        @Override
        public List<Element> annotatedElement() { return annotatedElement.stream().collect(Collectors.toList());}

        @Override
        public List<Comment> ownedComment() { return element.ownedComment().stream().collect(Collectors.toList()); }

        @Override
        public List<Element> ownedElement() { return element.ownedElement(); }

        @Override
        public Element owner() { return element.owner(); }
    }
}
