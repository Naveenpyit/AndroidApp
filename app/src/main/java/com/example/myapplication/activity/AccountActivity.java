package com.example.myapplication.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);

//        1.cart add
//url : https://www.tomhiddleb2b.com/api/add-cart
//request : {"n_category":"1","n_pack":"2","n_product":"1","n_qty":"0","n_user":"10"}
//
//2.list cart
//url : https://www.tomhiddleb2b.com/api/list-cart
//request
//{
//    "n_user":"10"
//}
//response
//{
//    "n_status": 1,
//    "c_message": "Cart Data Found",
//    "j_data": [
//        {
//            "n_id": "229",
//            "user_id": "10",
//            "n_category": "1",
//            "n_product": "3",
//            "n_pack": "2",
//            "n_mrp": "2796.00",
//            "category_name": "MEN",
//            "n_price": "822.00",
//            "n_quantity": "2",
//            "n_total": "1644.00",
//            "c_random": "Egvm5RZ6QJ2Y",
//            "c_item_name": "TOM HIDDLE BACK PRINT TSHIRT",
//            "n_gst": "5.00",
//            "dt_created": "2026-03-06 19:45:56",
//            "c_item_code": "A4-BP-0022-M-2XL",
//            "c_pack_name": "M-2XL",
//            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0022/1.webp",
//            "n_gst_value": "41.10"
//        }
//    ]
//}
//
//3.delete cart
//url : https://www.tomhiddleb2b.com/api/delete-cart
//request : {"n_cart":"106","n_user":"10"}
//
//4.update cart
//url : https://www.tomhiddleb2b.com/api/update-cart
//request
//{
//    "n_user":"4",
//    "n_cart":"15",
//    "n_product":"5",
//    "n_pack":"1",
//    "n_qty":"2"
//}
//
//5.filter api
//url : https://www.tomhiddleb2b.com/api/items-filters
//request
//{
//    "n_category":"1", // if category fiter
//    "n_user":"1"
//}
//response
//{
//    "n_status": 1,
//    "c_message": "Data Found",
//    "j_data": {
//        "j_headings": [
//            "Category",
//            "Section",
//            "Packs",
//            "Fit",
//            "Price",
//            "Tags"
//        ],
//        "j_category_filter": [
//            {
//                "n_id": "1",
//                "n_parent": null,
//                "c_name": "MEN",
//                "c_short_name": "men",
//                "product_count": "245"
//            },
//            {
//                "n_id": "7",
//                "n_parent": null,
//                "c_name": "WOMEN",
//                "c_short_name": "women",
//                "product_count": "20"
//            }
//        ],
//        "j_fit_filter": [
//            {
//                "id": "164",
//                "c_fit": "OVERSIZED",
//                "fit_count": "101"
//            },
//            {
//                "id": "1",
//                "c_fit": "SLIM",
//                "fit_count": "164"
//            }
//        ],
//        "j_price_filter": [
//            {
//                "c_price_range": "Below ₹500",
//                "n_value": "0-499"
//            },
//            {
//                "c_price_range": "₹500 - ₹999",
//                "n_value": "500-999"
//            },
//            {
//                "c_price_range": "₹1000 - ₹1499",
//                "n_value": "1000-1499"
//            },
//            {
//                "c_price_range": "₹1500 - ₹1999",
//                "n_value": "1500-1999"
//            },
//            {
//                "c_price_range": "₹2000 & Above",
//                "n_value": "2000-9999"
//            }
//        ],
//        "j_tags_filter": [
//            {
//                "c_tag": "GRAPHIC TSHIRT",
//                "n_id": "1"
//            }
//        ],
//        "j_section_filter": [],
//        "j_packs_filter": []
//    }
//}

//        1.add whislist
//url : https://www.tomhiddleb2b.com/api/add-wishlist
//request
//{
//    "n_category":"1",
//    "n_product":"6",
//    "n_pack":"5",
//    "n_user":"4"
//}
//response
//{
//    "n_status": 1,
//    "c_message": "Wishlist Added",
//    "n_wishlist_count": "3",
//    "n_cart_count": "1",
//    "j_data": []
//}
//
//2.list whislist
//url : https://www.tomhiddleb2b.com/api/list-wishlist
//request
//{
//    "n_user":"10"
//}
//response
//{
//    "n_status": 1,
//    "c_message": "Wishlist Data Found",
//    "j_data": [
//        {
//            "n_id": "11",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "8",
//            "n_status": "1",
//            "d_created": "2025-12-11 18:02:06",
//            "n_gst": "5.00",
//            "c_pack_name": "M-2XL",
//            "n_mrp": "2796.00",
//            "n_selling_price": "822.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-BP-0039-M-2XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0039/1.webp",
//            "n_gst_value": "41.10"
//        },
//        {
//            "n_id": "12",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "10",
//            "n_status": "1",
//            "d_created": "2025-12-11 18:02:08",
//            "n_gst": "5.00",
//            "c_pack_name": "M-2XL",
//            "n_mrp": "2796.00",
//            "n_selling_price": "822.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-BP-0044-M-2XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0044/1.webp",
//            "n_gst_value": "41.10"
//        },
//        {
//            "n_id": "21",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "259",
//            "n_status": "1",
//            "d_created": "2025-12-13 12:46:06",
//            "n_gst": "5.00",
//            "c_pack_name": "S-L",
//            "n_mrp": "4497.00",
//            "n_selling_price": "649.50",
//            "category_name": "MEN",
//            "c_item_code": "A3-OS-345-S-L",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/OS-345/1.webp",
//            "n_gst_value": "32.48"
//        },
//        {
//            "n_id": "27",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "256",
//            "n_status": "1",
//            "d_created": "2025-12-13 13:46:52",
//            "n_gst": "5.00",
//            "c_pack_name": "S-XL",
//            "n_mrp": "5996.00",
//            "n_selling_price": "866.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-OS-0363-S-XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/OS-0363/1.webp",
//            "n_gst_value": "43.30"
//        },
//        {
//            "n_id": "35",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "9",
//            "n_status": "1",
//            "d_created": "2025-12-17 19:34:39",
//            "n_gst": "5.00",
//            "c_pack_name": "M-2XL",
//            "n_mrp": "2796.00",
//            "n_selling_price": "822.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-BP-0042-M-2XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0042/1.webp",
//            "n_gst_value": "41.10"
//        },
//        {
//            "n_id": "39",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "17",
//            "n_status": "1",
//            "d_created": "2025-12-19 21:22:27",
//            "n_gst": "5.00",
//            "c_pack_name": "M-2XL",
//            "n_mrp": "2796.00",
//            "n_selling_price": "822.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-BP-0082-M-2XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0082/1.webp",
//            "n_gst_value": "41.10"
//        },
//        {
//            "n_id": "40",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "18",
//            "n_status": "1",
//            "d_created": "2025-12-19 21:22:29",
//            "n_gst": "5.00",
//            "c_pack_name": "M-2XL",
//            "n_mrp": "2796.00",
//            "n_selling_price": "822.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-BP-0083-M-2XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0083/1.webp",
//            "n_gst_value": "41.10"
//        },
//        {
//            "n_id": "43",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "6",
//            "n_status": "1",
//            "d_created": "2025-12-19 21:28:03",
//            "n_gst": "5.00",
//            "c_pack_name": "M-2XL",
//            "n_mrp": "2796.00",
//            "n_selling_price": "822.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-BP-0036-M-2XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0036/1.webp",
//            "n_gst_value": "41.10"
//        },
//        {
//            "n_id": "51",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "248",
//            "n_status": "1",
//            "d_created": "2025-12-20 19:45:02",
//            "n_gst": "5.00",
//            "c_pack_name": "S-XL",
//            "n_mrp": "5996.00",
//            "n_selling_price": "866.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-OS-0382-S-XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/OS-0382/1.webp",
//            "n_gst_value": "43.30"
//        },
//        {
//            "n_id": "52",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "247",
//            "n_status": "1",
//            "d_created": "2025-12-20 19:45:23",
//            "n_gst": "5.00",
//            "c_pack_name": "S-XL",
//            "n_mrp": "5996.00",
//            "n_selling_price": "866.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-OS-0383-S-XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/OS-0383/1.webp",
//            "n_gst_value": "43.30"
//        },
//        {
//            "n_id": "53",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "246",
//            "n_status": "1",
//            "d_created": "2025-12-20 19:47:52",
//            "n_gst": "5.00",
//            "c_pack_name": "S-XL",
//            "n_mrp": "5996.00",
//            "n_selling_price": "866.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-OS-0386-S-XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/OS-0386/1.webp",
//            "n_gst_value": "43.30"
//        },
//        {
//            "n_id": "55",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "255",
//            "n_status": "1",
//            "d_created": "2025-12-20 19:54:52",
//            "n_gst": "5.00",
//            "c_pack_name": "S-L",
//            "n_mrp": "4497.00",
//            "n_selling_price": "649.50",
//            "category_name": "MEN",
//            "c_item_code": "A3-OS-0364-S-L",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/OS-0364/1.webp",
//            "n_gst_value": "32.48"
//        },
//        {
//            "n_id": "56",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "254",
//            "n_status": "1",
//            "d_created": "2025-12-20 19:55:14",
//            "n_gst": "5.00",
//            "c_pack_name": "S-XL",
//            "n_mrp": "5996.00",
//            "n_selling_price": "866.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-OS-0365-S-XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/OS-0365/1.webp",
//            "n_gst_value": "43.30"
//        },
//        {
//            "n_id": "64",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "2",
//            "n_status": "1",
//            "d_created": "2025-12-24 21:08:59",
//            "n_gst": "5.00",
//            "c_pack_name": "M-2XL",
//            "n_mrp": "2796.00",
//            "n_selling_price": "822.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-BP-0020-M-2XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/M-2XL/1.webp",
//            "n_gst_value": "41.10"
//        },
//        {
//            "n_id": "66",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "56",
//            "n_status": "1",
//            "d_created": "2026-01-11 04:09:59",
//            "n_gst": "5.00",
//            "c_pack_name": "M-2XL",
//            "n_mrp": "2796.00",
//            "n_selling_price": "822.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-BP-0140-M-2XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/M-2XL/1.webp",
//            "n_gst_value": "41.10"
//        },
//        {
//            "n_id": "67",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "57",
//            "n_status": "1",
//            "d_created": "2026-01-11 04:10:01",
//            "n_gst": "5.00",
//            "c_pack_name": "M-2XL",
//            "n_mrp": "2796.00",
//            "n_selling_price": "822.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-BP-0141-M-2XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0141/1.webp",
//            "n_gst_value": "41.10"
//        },
//        {
//            "n_id": "82",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "7",
//            "n_status": "1",
//            "d_created": "2026-03-06 18:37:59",
//            "n_gst": "5.00",
//            "c_pack_name": "M-2XL",
//            "n_mrp": "2796.00",
//            "n_selling_price": "822.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-BP-0038-M-2XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0038/1.webp",
//            "n_gst_value": "41.10"
//        },
//        {
//            "n_id": "86",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "4",
//            "n_status": "1",
//            "d_created": "2026-03-06 18:52:28",
//            "n_gst": "5.00",
//            "c_pack_name": "M-2XL",
//            "n_mrp": "2796.00",
//            "n_selling_price": "822.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-BP-0028-M-2XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0028/1.webp",
//            "n_gst_value": "41.10"
//        },
//        {
//            "n_id": "88",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "11",
//            "n_status": "1",
//            "d_created": "2026-03-06 18:54:42",
//            "n_gst": "5.00",
//            "c_pack_name": "M-2XL",
//            "n_mrp": "2796.00",
//            "n_selling_price": "822.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-BP-0052-M-2XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0052/1.webp",
//            "n_gst_value": "41.10"
//        },
//        {
//            "n_id": "89",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "12",
//            "n_status": "1",
//            "d_created": "2026-03-06 18:55:10",
//            "n_gst": "5.00",
//            "c_pack_name": "M-2XL",
//            "n_mrp": "2796.00",
//            "n_selling_price": "822.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-BP-0055-M-2XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0055/1.webp",
//            "n_gst_value": "41.10"
//        },
//        {
//            "n_id": "90",
//            "n_customer": "10",
//            "n_category": "1",
//            "n_product": "13",
//            "n_status": "1",
//            "d_created": "2026-03-06 18:55:32",
//            "n_gst": "5.00",
//            "c_pack_name": "M-2XL",
//            "n_mrp": "2796.00",
//            "n_selling_price": "822.00",
//            "category_name": "MEN",
//            "c_item_code": "A4-BP-0056-M-2XL",
//            "c_fabric": "SINGLE JERSEY 190GSM",
//            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0056/1.webp",
//            "n_gst_value": "41.10"
//        }
//    ]
//}
//
//3.delete wishlist
//url : https://www.tomhiddleb2b.com/api/delete-wishlist
//request
//{
//    "n_user":"4",
//    "n_wishlist":"7"
//}

    }
}