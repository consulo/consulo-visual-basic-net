Module Xml
    Sub Main()
        Dim a = <root attr="v"><child/><%= x + 1 %></root>
        Dim b = <?xml version="1.0"?>
                <doc>
                    <item name=<%= GetName() %>/>
                </doc>
        Dim c = <!-- only --> : Call <?pi data?>()
        Dim d = x.<child>.@attr & x...<desc>.Value & x.@<ns:name>
        Dim e = If(x, <a>it's</a>, <b/>)
    End Sub

    Sub F(<Out> ByRef y As Integer)
    End Sub
End Module
