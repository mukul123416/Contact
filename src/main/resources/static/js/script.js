const toggleSidebar = () => {
   if($(".sidebar").is(":visible")){
       $(".sidebar").css("display","none");
       $(".content").css("margin-left","0%");
    } else {
      $(".sidebar").css("display","block");
      $(".content").css("margin-left","20%");
    }
   };



const search = () => {

  let query = $("#search-input").val();

  if(query == ""){
    $(".search-result").hide();
  }
  else{
    
    let url = `http://localhost:8080/search/${query}`;

    fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((data) => {
         let text = `<div class='list-group'>`;
         data.forEach((contact) => {
           text += `<a href='/user/contact/${contact.cId}' class='list-group-item list-group-item-action'> ${contact.name} </a>`
         });
         text += `</div>`;
         $(".search-result").html(text);
         $(".search-result").show();
      });

  }
};



//first request to server to create order
const paymentStart = () =>{
  console.log("Payment started..");
  var amount = $("#payment_field").val();
  console.log(amount);
  if(amount == "" || amount == null){
    swal("Failed!", "amount is required !!", "error");
    return;
  }

  $.ajax({
    url:'/user/create_order',
    data:JSON.stringify({amount:amount, info:"order_request"}),
    contentType:'application/json',
    type:'POST',
    dataType:'json',
    success:function(response){
      console.log(response);
      if(response.status == 'created'){

        let options={
          key: "rzp_test_CzxcPqUG4CiCM9",
          amount: "response.amount",
          currency: "INR",
          name: "Smart Contact Manager",
          description: "Donation",
          image: "https://storage.googleapis.com/proudcity/sanrafaelca/uploads/2020/04/donate-image.png",
          order_id:response.id,
          handler: function (response) {
            console.log(response.razorpay_payment_id);
            console.log(response.razorpay_order_id);
            console.log(response.razorpay_signature);
            updatePaymentOnServer(response.razorpay_payment_id,response.razorpay_order_id,"Paid");
          },
          prefill: {
            name: "",
            email: "",
            contact: ""
          },
          notes: {
            address: "Razorpay Corporate Office"
          },
          theme: {
            color: "#3399cc"
          }
        };

        let rzp=new Razorpay(options);
        rzp.open();
        rzp.on('payment.failed', function (response){
          console.log(response.error.code);
          console.log(response.error.description);
          console.log(response.error.source);
          console.log(response.error.step);
          console.log(response.error.reason);
          console.log(response.error.metadata.order_id);
          console.log(response.error.metadata.payment_id);
          swal("Failed!", "Oops payment failed !!", "error");
       });

      }
    },
    error:function(error){
      console.log(error);
      swal("Failed !!", "something went wrong !!", "error");
    }
  })
};



function updatePaymentOnServer(Payment_id,Order_id,Status)
{
  $.ajax({
    url:'/user/update_order',
    data:JSON.stringify({Payment_id:Payment_id, Order_id:Order_id,Status:Status}),
    contentType:'application/json',
    type:'POST',
    dataType:'json',
    success:function(response){
      console.log("payment successful !!");
      swal("Good job!", "congrats !! Payment successful !!", "success");
    },
    error:function(error){
      swal("Failed !!", "Your payment is successful , but we did't get on server , we will contact you as soon as possible", "error");
    }
  });
}