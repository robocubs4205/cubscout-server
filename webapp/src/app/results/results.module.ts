import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {ResultsComponent, FilterSections, SortResults} from "./results.component";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {HttpModule, JsonpModule} from "@angular/http";
import {MaterialModule} from "@angular/material";
import {RatingModule} from "../rating/rating.module";

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    JsonpModule,
    MaterialModule,
    CommonModule,
    RatingModule
  ],
  declarations: [
    ResultsComponent,
    FilterSections,
    SortResults
  ],
  exports:[
    ResultsComponent
  ]
})
export class ResultsModule { }
