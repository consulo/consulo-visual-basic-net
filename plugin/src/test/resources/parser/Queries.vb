Module Queries
    Sub Main()
        Dim q = From c In customers, o In orders
                Where c.Id = o.CustomerId
                Order By c.Name Descending, o.Total
                Select c.Name, Total = o.Total
        Dim g = From c In customers
                Group c By c.Country Into Count(), Total = Sum(c.Balance)
        Dim j = From c In customers
                Join o In orders On c.Id Equals o.CustomerId
                Let total = o.Total * 2
                Distinct
                Skip 1
                Take While total > 0
                Select total
        Dim a = Aggregate o In orders Into Sum(o.Total)
        Dim gj = From c In customers
                 Group Join o In orders On c.Id Equals o.CustomerId Into Orders = Group
    End Sub
End Module
